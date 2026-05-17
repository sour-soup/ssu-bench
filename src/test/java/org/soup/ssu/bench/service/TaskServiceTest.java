package org.soup.ssu.bench.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.soup.ssu.bench.exception.BadRequestException;
import org.soup.ssu.bench.exception.EntityNotFoundException;
import org.soup.ssu.bench.exception.ForbiddenException;
import org.soup.ssu.bench.repository.BidRepository;
import org.soup.ssu.bench.repository.PaymentRepository;
import org.soup.ssu.bench.repository.TaskRepository;
import org.soup.ssu.bench.repository.UserRepository;
import org.soup.ssu.bench.repository.entity.PaymentEntity;
import org.soup.ssu.bench.repository.entity.TaskEntity;
import org.soup.ssu.bench.repository.entity.UserEntity;
import ssu.bench.model.BidStatusEnum;
import ssu.bench.model.CreateTaskRequest;
import ssu.bench.model.PageTaskResponse;
import ssu.bench.model.TaskResponse;
import ssu.bench.model.TaskStatusEnum;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.soup.ssu.bench.generator.EntityGenerator.CUSTOMER_ID;
import static org.soup.ssu.bench.generator.EntityGenerator.TASK_ID;
import static org.soup.ssu.bench.generator.EntityGenerator.buildTaskEntity;
import static org.soup.ssu.bench.generator.EntityGenerator.buildUserEntity;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private BidRepository bidRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskService taskService;

    @Test
    void givenTask_whenCreateTask_thenReturnTaskWithId() {
        // given
        TaskEntity taskEntity = buildTaskEntity();
        when(taskRepository.createTask(taskEntity)).thenReturn(taskEntity.withId(TASK_ID));

        // when
        TaskResponse taskResponse = taskService.createTask(CUSTOMER_ID, buildCreateTaskRequest(taskEntity));

        // then
        assertTaskMapping(taskEntity, taskResponse);
    }

    @Test
    void givenExistingTaskId_whenGetTaskById_thenReturnTask() {
        // given
        TaskEntity taskEntity = buildTaskEntity().withId(TASK_ID);
        when(taskRepository.getTaskById(TASK_ID)).thenReturn(Optional.of(taskEntity));

        // when
        TaskResponse taskResponse = taskService.getTaskById(TASK_ID);

        // then
        assertTaskMapping(taskEntity, taskResponse);
    }

    @Test
    void givenNonExistingTaskId_whenGetTaskById_thenThrowEntityNotFoundException() {
        // given
        when(taskRepository.getTaskById(TASK_ID)).thenReturn(Optional.empty());

        // when & then
        assertThrows(EntityNotFoundException.class, () -> taskService.getTaskById(TASK_ID));
    }

    @Test
    void givenTasksExist_whenGetListTasks_thenReturnPageTaskResponse() {
        // given
        int page = 0;
        int size = 10;
        TaskStatusEnum status = TaskStatusEnum.PUBLISHED;

        TaskEntity taskEntity1 = buildTaskEntity().withId(BigInteger.ONE);
        TaskEntity taskEntity2 = buildTaskEntity().withId(BigInteger.TWO);
        List<TaskEntity> taskEntities = List.of(taskEntity1, taskEntity2);

        when(taskRepository.getTasks(page, size, status.getValue())).thenReturn(taskEntities);

        // when
        PageTaskResponse response = taskService.getListTasks(page, size, status);

        // then
        assertNotNull(response);
        assertEquals(page, response.getPage());
        assertEquals(size, response.getSize());
        assertEquals(2, response.getContent().size());
    }

    @Test
    void givenNoTasks_whenGetListTasks_thenReturnEmptyPageTaskResponse() {
        // given
        int page = 0;
        int size = 10;
        TaskStatusEnum status = TaskStatusEnum.PUBLISHED;

        when(taskRepository.getTasks(page, size, status.getValue())).thenReturn(List.of());

        // when
        PageTaskResponse response = taskService.getListTasks(page, size, status);

        // then
        assertNotNull(response);
        assertEquals(0, response.getContent().size());
    }

    @Test
    void givenTaskInProgress_whenCancelTask_thenCancelAndRefund() {
        // given
        TaskEntity taskEntity = buildTaskEntity()
            .withId(TASK_ID)
            .withCustomerId(CUSTOMER_ID)
            .withStatus(TaskStatusEnum.IN_PROGRESS.getValue())
            .withReward(BigInteger.valueOf(1000));

        UserEntity customer = buildUserEntity().withId(CUSTOMER_ID).withBalance(BigInteger.valueOf(5000));
        TaskEntity updatedTaskEntity = taskEntity.withStatus(TaskStatusEnum.CANCELLED.getValue());

        when(taskRepository.getTaskById(TASK_ID)).thenReturn(Optional.of(taskEntity));
        when(userRepository.getUserById(CUSTOMER_ID)).thenReturn(Optional.of(customer));
        when(taskRepository.updateStatus(TASK_ID, TaskStatusEnum.CANCELLED.getValue())).thenReturn(updatedTaskEntity);

        // when
        TaskResponse response = taskService.cancelTask(TASK_ID, CUSTOMER_ID);

        // then
        assertEquals(TaskStatusEnum.CANCELLED.getValue(), response.getStatus().getValue());
        verify(paymentRepository).createPayment(any(PaymentEntity.class));
        verify(userRepository).updateBalance(CUSTOMER_ID, customer.balance().add(taskEntity.reward()));
        verify(taskRepository).updateStatus(TASK_ID, TaskStatusEnum.CANCELLED.getValue());
        verify(bidRepository).updateStatusByTaskId(TASK_ID, BidStatusEnum.REJECTED.getValue());
    }

    @Test
    void givenTaskNotInProgress_whenCancelTask_thenCancelWithoutRefund() {
        // given
        TaskEntity taskEntity = buildTaskEntity()
            .withId(TASK_ID)
            .withCustomerId(CUSTOMER_ID)
            .withStatus(TaskStatusEnum.PUBLISHED.getValue());

        UserEntity customer = buildUserEntity().withId(CUSTOMER_ID);
        TaskEntity updatedTaskEntity = taskEntity.withStatus(TaskStatusEnum.CANCELLED.getValue());

        when(taskRepository.getTaskById(TASK_ID)).thenReturn(Optional.of(taskEntity));
        when(userRepository.getUserById(CUSTOMER_ID)).thenReturn(Optional.of(customer));
        when(taskRepository.updateStatus(TASK_ID, TaskStatusEnum.CANCELLED.getValue())).thenReturn(updatedTaskEntity);

        // when
        TaskResponse response = taskService.cancelTask(TASK_ID, CUSTOMER_ID);

        // then
        assertEquals(TaskStatusEnum.CANCELLED.getValue(), response.getStatus().getValue());
        verify(paymentRepository, never()).createPayment(any(PaymentEntity.class));
        verify(userRepository, never()).updateBalance(any(), any());
        verify(taskRepository).updateStatus(TASK_ID, TaskStatusEnum.CANCELLED.getValue());
        verify(bidRepository).updateStatusByTaskId(TASK_ID, BidStatusEnum.REJECTED.getValue());
    }

    @Test
    void givenNonExistingTask_whenCancelTask_thenThrowEntityNotFoundException() {
        // given
        when(taskRepository.getTaskById(TASK_ID)).thenReturn(Optional.empty());

        // when & then
        assertThrows(EntityNotFoundException.class, () -> taskService.cancelTask(TASK_ID, CUSTOMER_ID));
        verify(taskRepository).getTaskById(TASK_ID);
        verifyNoInteractions(paymentRepository, userRepository);
    }

    @Test
    void givenNonExistingUser_whenCancelTask_thenThrowEntityNotFoundException() {
        // given
        TaskEntity taskEntity = buildTaskEntity().withId(TASK_ID).withCustomerId(CUSTOMER_ID);
        when(taskRepository.getTaskById(TASK_ID)).thenReturn(Optional.of(taskEntity));
        when(userRepository.getUserById(CUSTOMER_ID)).thenReturn(Optional.empty());

        // when & then
        assertThrows(EntityNotFoundException.class, () -> taskService.cancelTask(TASK_ID, CUSTOMER_ID));
    }

    @Test
    void givenDifferentCustomer_whenCancelTask_thenThrowForbiddenException() {
        // given
        BigInteger differentCustomerId = BigInteger.valueOf(999);
        TaskEntity taskEntity = buildTaskEntity().withId(TASK_ID).withCustomerId(CUSTOMER_ID);

        when(taskRepository.getTaskById(TASK_ID)).thenReturn(Optional.of(taskEntity));
        when(userRepository.getUserById(differentCustomerId)).thenReturn(Optional.of(buildUserEntity().withId(differentCustomerId)));

        // when & then
        assertThrows(ForbiddenException.class, () -> taskService.cancelTask(TASK_ID, differentCustomerId));
        verify(taskRepository, never()).updateStatus(any(), any());
    }

    @Test
    void givenTaskInProgress_whenConfirmTask_thenConfirmAndCharge() {
        // given
        BigInteger executorId = BigInteger.valueOf(200);
        TaskEntity taskEntity = buildTaskEntity()
            .withId(TASK_ID)
            .withCustomerId(CUSTOMER_ID)
            .withExecutorId(executorId)
            .withStatus(TaskStatusEnum.IN_PROGRESS.getValue())
            .withReward(BigInteger.valueOf(1000));

        UserEntity executor = buildUserEntity().withId(executorId).withBalance(BigInteger.valueOf(5000));
        TaskEntity updatedTaskEntity = taskEntity.withStatus(TaskStatusEnum.COMPLETED.getValue());

        when(taskRepository.getTaskById(TASK_ID)).thenReturn(Optional.of(taskEntity));
        when(userRepository.getUserById(executorId)).thenReturn(Optional.of(executor));
        when(taskRepository.updateStatus(TASK_ID, TaskStatusEnum.COMPLETED.getValue())).thenReturn(updatedTaskEntity);

        // when
        TaskResponse response = taskService.confirmTask(TASK_ID, CUSTOMER_ID);

        // then
        assertEquals(TaskStatusEnum.COMPLETED.getValue(), response.getStatus().getValue());
        verify(paymentRepository).createPayment(any(PaymentEntity.class));
        verify(userRepository).updateBalance(executorId, executor.balance().add(taskEntity.reward()));
        verify(taskRepository).updateStatus(TASK_ID, TaskStatusEnum.COMPLETED.getValue());
    }

    @Test
    void givenTaskNotInProgress_whenConfirmTask_thenThrowBadRequestException() {
        // given
        TaskEntity taskEntity = buildTaskEntity()
            .withId(TASK_ID)
            .withCustomerId(CUSTOMER_ID)
            .withStatus(TaskStatusEnum.PUBLISHED.getValue());

        when(taskRepository.getTaskById(TASK_ID)).thenReturn(Optional.of(taskEntity));

        // when & then
        assertThrows(BadRequestException.class, () -> taskService.confirmTask(TASK_ID, CUSTOMER_ID));
        verify(taskRepository).getTaskById(TASK_ID);
        verify(paymentRepository, never()).createPayment(any());
        verify(userRepository, never()).updateBalance(any(), any());
    }

    @Test
    void givenNonExistingTask_whenConfirmTask_thenThrowEntityNotFoundException() {
        // given
        when(taskRepository.getTaskById(TASK_ID)).thenReturn(Optional.empty());

        // when & then
        assertThrows(EntityNotFoundException.class, () -> taskService.confirmTask(TASK_ID, CUSTOMER_ID));
        verify(taskRepository).getTaskById(TASK_ID);
    }

    @Test
    void givenDifferentCustomer_whenConfirmTask_thenThrowForbiddenException() {
        // given
        BigInteger differentCustomerId = BigInteger.valueOf(999);
        TaskEntity taskEntity = buildTaskEntity().withId(TASK_ID).withCustomerId(CUSTOMER_ID);

        when(taskRepository.getTaskById(TASK_ID)).thenReturn(Optional.of(taskEntity));

        // when & then
        assertThrows(ForbiddenException.class, () -> taskService.confirmTask(TASK_ID, differentCustomerId));
        verify(taskRepository).getTaskById(TASK_ID);
        verify(paymentRepository, never()).createPayment(any());
    }

    @Test
    void givenExecutorNotFound_whenConfirmTask_thenThrowInternalErrorException() {
        // given
        BigInteger executorId = BigInteger.valueOf(200);
        TaskEntity taskEntity = buildTaskEntity()
            .withId(TASK_ID)
            .withCustomerId(CUSTOMER_ID)
            .withExecutorId(executorId)
            .withStatus(TaskStatusEnum.IN_PROGRESS.getValue());

        when(taskRepository.getTaskById(TASK_ID)).thenReturn(Optional.of(taskEntity));
        when(userRepository.getUserById(executorId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(
            org.soup.ssu.bench.exception.InternalErrorException.class,
            () -> taskService.confirmTask(TASK_ID, CUSTOMER_ID)
        );
        verify(taskRepository).getTaskById(TASK_ID);
        verify(userRepository).getUserById(executorId);
        verify(paymentRepository, never()).createPayment(any());
    }


    private static void assertTaskMapping(TaskEntity taskEntity, TaskResponse taskResponse) {
        assertEquals(taskEntity.title(), taskResponse.getTitle());
        assertEquals(taskEntity.description(), taskResponse.getDescription());
        assertEquals(taskEntity.reward(), taskResponse.getReward());
        assertEquals(taskEntity.status(), taskResponse.getStatus().getValue());
    }

    private static CreateTaskRequest buildCreateTaskRequest(TaskEntity taskEntity) {
        return new CreateTaskRequest()
            .title(taskEntity.title())
            .description(taskEntity.description())
            .reward(taskEntity.reward());
    }
}
