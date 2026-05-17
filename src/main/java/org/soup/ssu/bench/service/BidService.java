package org.soup.ssu.bench.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.compare.ComparableUtils;
import org.soup.ssu.bench.exception.BadRequestException;
import org.soup.ssu.bench.exception.EntityNotFoundException;
import org.soup.ssu.bench.exception.ForbiddenException;
import org.soup.ssu.bench.exception.InternalErrorException;
import org.soup.ssu.bench.repository.BidRepository;
import org.soup.ssu.bench.repository.PaymentRepository;
import org.soup.ssu.bench.repository.TaskRepository;
import org.soup.ssu.bench.repository.UserRepository;
import org.soup.ssu.bench.repository.entity.BidEntity;
import org.soup.ssu.bench.repository.entity.PaymentEntity;
import org.soup.ssu.bench.repository.entity.TaskEntity;
import org.soup.ssu.bench.repository.entity.UserEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ssu.bench.model.BidResponse;
import ssu.bench.model.BidStatusEnum;
import ssu.bench.model.PageBidResponse;
import ssu.bench.model.PaymentTypeEnum;
import ssu.bench.model.TaskStatusEnum;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class BidService {
    private final TaskRepository taskRepository;
    private final BidRepository bidRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    public BidResponse getBidById(BigInteger bidId) {
        BidEntity bidEntity = bidRepository.getBidById(bidId)
            .orElseThrow(() -> new EntityNotFoundException("Bid", bidId));

        return mapBidEntityToResponse(bidEntity);
    }

    public PageBidResponse getTaskBids(BigInteger taskId, Integer page, Integer size) {
        List<BidResponse> bidResponses = bidRepository.getBids(taskId, page, size).stream()
            .map(this::mapBidEntityToResponse)
            .toList();

        return new PageBidResponse()
            .content(bidResponses)
            .page(page)
            .size(size);
    }

    public BidResponse createBid(BigInteger taskId, BigInteger executorId) {
        BidEntity bidEntity = bidRepository.createBid(buildBidEntity(taskId, executorId));

        return mapBidEntityToResponse(bidEntity);
    }

    @Transactional
    public BidResponse acceptBid(BigInteger bidId, BigInteger executorId) {
        BidEntity bidEntity = getAndValidateBid(bidId, executorId);
        TaskEntity taskEntity = getAndValidateTask(bidEntity.taskId());
        UserEntity executor = getAndValidateExecutorBalance(executorId, taskEntity.reward());

        executeAcceptanceTransaction(bidEntity, taskEntity, executor);

        return mapBidEntityToResponse(bidEntity);
    }

    private BidEntity getAndValidateBid(BigInteger bidId, BigInteger executorId) {
        BidEntity bidEntity = bidRepository.getBidById(bidId)
            .orElseThrow(() -> new EntityNotFoundException("Bid", bidId));

        if (!bidEntity.executorId().equals(executorId)) {
            throw new ForbiddenException("You are not the executor of this bid");
        }

        if (!bidEntity.status().equals(BidStatusEnum.PENDING.getValue())) {
            throw new BadRequestException("Bid status must be PENDING, current status: " +
                                          BidStatusEnum.fromValue(bidEntity.status()));
        }

        return bidEntity;
    }

    private TaskEntity getAndValidateTask(BigInteger taskId) {
        TaskEntity taskEntity = taskRepository.getTaskById(taskId)
            .orElseThrow(() -> new InternalErrorException("Task not found for ID: " + taskId));

        if (!taskEntity.status().equals(TaskStatusEnum.PUBLISHED.getValue())) {
            throw new BadRequestException("Task status must be PUBLISHED, current status: " +
                                          TaskStatusEnum.fromValue(taskEntity.status()));
        }

        return taskEntity;
    }

    private UserEntity getAndValidateExecutorBalance(BigInteger executorId, BigInteger requiredReward) {
        UserEntity executor = userRepository.getUserById(executorId)
            .orElseThrow(() -> new InternalErrorException("Executor not found for ID: " + executorId));

        if (executor.balance().compareTo(requiredReward) < 0) {
            throw new BadRequestException(
                String.format("Insufficient balance. Required: %d, Available: %d",
                    requiredReward, executor.balance())
            );
        }

        return executor;
    }

    private void executeAcceptanceTransaction(BidEntity bidEntity, TaskEntity taskEntity, UserEntity executor) {
        taskRepository.updateStatus(taskEntity.id(), TaskStatusEnum.IN_PROGRESS.getValue());

        bidRepository.updateStatusByTaskId(taskEntity.id(), BidStatusEnum.REJECTED.getValue());
        bidRepository.updateStatus(bidEntity.id(), BidStatusEnum.ACCEPTED.getValue());

        userRepository.updateBalance(executor.id(), executor.balance().subtract(taskEntity.reward()));
        paymentRepository.createPayment(buildHoldPaymentEntity(taskEntity));

        log.info("Bid {} accepted for task {}. Amount {} held from executor {}",
            bidEntity.id(), taskEntity.id(), taskEntity.reward(), executor.id());
    }

    private PaymentEntity buildHoldPaymentEntity(TaskEntity taskEntity) {
        return PaymentEntity.builder()
            .taskId(taskEntity.id())
            .senderId(taskEntity.customerId())
            .receiverId(taskEntity.executorId())
            .amount(taskEntity.reward())
            .type(PaymentTypeEnum.HOLD.getValue())
            .build();
    }

    private BidEntity buildBidEntity(BigInteger taskId, BigInteger executorId) {
        return BidEntity.builder()
            .taskId(taskId)
            .executorId(executorId)
            .status(BidStatusEnum.PENDING.getValue())
            .createdAt(LocalDateTime.now())
            .build();
    }

    private BidResponse mapBidEntityToResponse(BidEntity bidEntity) {
        return new BidResponse()
            .id(bidEntity.id())
            .taskId(bidEntity.taskId())
            .status(BidStatusEnum.fromValue(bidEntity.status()))
            .executorId(bidEntity.executorId())
            .createdAt(bidEntity.createdAt());
    }
}
