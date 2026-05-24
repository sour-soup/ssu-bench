package org.soup.ssu.bench.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.soup.ssu.bench.exception.BadRequestException;
import org.soup.ssu.bench.exception.EntityNotFoundException;
import org.soup.ssu.bench.exception.ForbiddenException;
import org.soup.ssu.bench.exception.InternalErrorException;
import org.soup.ssu.bench.repository.BidRepository;
import org.soup.ssu.bench.repository.PaymentRepository;
import org.soup.ssu.bench.repository.TaskRepository;
import org.soup.ssu.bench.repository.UserRepository;
import org.soup.ssu.bench.repository.entity.PaymentEntity;
import org.soup.ssu.bench.repository.entity.TaskEntity;
import org.soup.ssu.bench.repository.entity.UserEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ssu.bench.model.BidStatusEnum;
import ssu.bench.model.CreateTaskRequest;
import ssu.bench.model.PageTaskResponse;
import ssu.bench.model.PaymentTypeEnum;
import ssu.bench.model.TaskResponse;
import ssu.bench.model.TaskStatusEnum;

import java.math.BigInteger;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final BidRepository bidRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    public TaskResponse createTask(BigInteger customerId, CreateTaskRequest createTaskRequest) {
        TaskEntity taskEntity = buildTaskEntity(customerId, createTaskRequest);

        return mapTaskEntityToResponse(taskRepository.createTask(taskEntity));
    }

    public TaskResponse getTaskById(BigInteger taskId) {
        TaskEntity taskEntity = taskRepository.getTaskById(taskId)
            .orElseThrow(() -> new EntityNotFoundException("Task", taskId));

        return mapTaskEntityToResponse(taskEntity);
    }

    public PageTaskResponse getListTasks(int page, int size, TaskStatusEnum status) {
        List<TaskResponse> taskResponses = taskRepository.getTasks(page, size, status.getValue()).stream()
            .map(TaskService::mapTaskEntityToResponse)
            .toList();

        return new PageTaskResponse()
            .content(taskResponses)
            .page(page)
            .size(size);
    }

    @Transactional
    public TaskResponse cancelTask(BigInteger taskId, BigInteger customerId) {
        TaskEntity taskEntity = getAndValidateTaskForCustomer(taskId, customerId);
        UserEntity customer = userRepository.getUserById(customerId)
            .orElseThrow(() -> new EntityNotFoundException("User", customerId));

        if (isTaskInProgress(taskEntity)) {
            processTaskCancellationRefund(taskEntity, customer);
        }

        return finalizeTaskCancellation(taskEntity);
    }

    public TaskResponse completeTask(BigInteger taskId, BigInteger customerId) {
        TaskEntity taskEntity = getAndValidateTaskForExecutor(taskId, customerId);

        if (!isTaskInProgress(taskEntity)) {
            throw new BadRequestException("Task is not in progress");
        }

        TaskEntity updatedTaskEntity = taskRepository.updateStatus(taskId, TaskStatusEnum.COMPLETED.getValue());
        return mapTaskEntityToResponse(updatedTaskEntity);
    }

    @Transactional
    public TaskResponse confirmTask(BigInteger taskId, BigInteger customerId) {
        TaskEntity taskEntity = getAndValidateTaskForCustomer(taskId, customerId);

        if (!isTaskCompleted(taskEntity)) {
            throw new BadRequestException("Task is not completed");
        }

        UserEntity executor = userRepository.getUserById(taskEntity.executorId())
            .orElseThrow(() -> new InternalErrorException("Executor not found"));

        processTaskCompletionPayment(taskEntity, executor);

        TaskEntity updatedTaskEntity = taskRepository.updateStatus(taskId, TaskStatusEnum.CONFIRMED.getValue());
        return mapTaskEntityToResponse(updatedTaskEntity);
    }

    private TaskEntity getAndValidateTaskForCustomer(BigInteger taskId, BigInteger customerId) {
        TaskEntity taskEntity = taskRepository.getTaskById(taskId)
            .orElseThrow(() -> new EntityNotFoundException("Task", taskId));

        if (!taskEntity.customerId().equals(customerId)) {
            throw new ForbiddenException("You are not a customer");
        }

        return taskEntity;
    }

    private TaskEntity getAndValidateTaskForExecutor(BigInteger taskId, BigInteger executorId) {
        TaskEntity taskEntity = taskRepository.getTaskById(taskId)
            .orElseThrow(() -> new EntityNotFoundException("Task", taskId));

        if (!taskEntity.executorId().equals(executorId)) {
            throw new ForbiddenException("You are not a executor");
        }

        return taskEntity;
    }

    private void processTaskCancellationRefund(TaskEntity taskEntity, UserEntity customer) {
        log.info(
            "Task {} cancelled. Refund of {} processed to customer {}",
            taskEntity.id(), taskEntity.reward(), customer.id()
        );
        paymentRepository.createPayment(buildPaymentEntity(taskEntity, PaymentTypeEnum.REFUND));
        userRepository.updateBalance(customer.id(), customer.balance().add(taskEntity.reward()));
    }

    private void processTaskCompletionPayment(TaskEntity taskEntity, UserEntity executor) {
        log.info(
            "Task {} completed. Payment of {} transferred to executor {}",
            taskEntity.id(), taskEntity.reward(), executor.id()
        );
        paymentRepository.createPayment(buildPaymentEntity(taskEntity, PaymentTypeEnum.CHARGE));
        userRepository.updateBalance(executor.id(), executor.balance().add(taskEntity.reward()));
    }

    private TaskResponse finalizeTaskCancellation(TaskEntity taskEntity) {
        TaskEntity updatedTaskEntity = taskRepository.updateStatus(taskEntity.id(), TaskStatusEnum.CANCELLED.getValue());
        bidRepository.updateStatusByTaskId(taskEntity.id(), BidStatusEnum.REJECTED.getValue());
        return mapTaskEntityToResponse(updatedTaskEntity);
    }

    private PaymentEntity buildPaymentEntity(TaskEntity taskEntity, PaymentTypeEnum paymentType) {
        return PaymentEntity.builder()
            .taskId(taskEntity.id())
            .senderId(taskEntity.customerId())
            .receiverId(taskEntity.executorId())
            .amount(taskEntity.reward())
            .type(paymentType.getValue())
            .build();
    }

    private boolean isTaskInProgress(TaskEntity taskEntity) {
        return TaskStatusEnum.IN_PROGRESS.getValue().equals(taskEntity.status());
    }

    private boolean isTaskCompleted(TaskEntity taskEntity) {
        return TaskStatusEnum.COMPLETED.getValue().equals(taskEntity.status());
    }

    private static TaskResponse mapTaskEntityToResponse(TaskEntity taskEntity) {
        return new TaskResponse()
            .id(taskEntity.id())
            .title(taskEntity.title())
            .description(taskEntity.description())
            .reward(taskEntity.reward())
            .status(TaskStatusEnum.fromValue(taskEntity.status()))
            .customerId(taskEntity.customerId())
            .executorId(taskEntity.executorId())
            .createdAt(taskEntity.createdAt())
            .updatedAt(taskEntity.updatedAt());
    }

    private static TaskEntity buildTaskEntity(BigInteger customerId, CreateTaskRequest createTaskRequest) {
        return TaskEntity.builder()
            .customerId(customerId)
            .title(createTaskRequest.getTitle())
            .description(createTaskRequest.getDescription())
            .reward(createTaskRequest.getReward())
            .status(TaskStatusEnum.PUBLISHED.getValue())
            .build();
    }
}
