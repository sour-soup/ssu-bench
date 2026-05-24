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
    public BidResponse acceptBid(BigInteger bidId, BigInteger customerId) {
        BidEntity bidEntity = getAndValidateBid(bidId);
        TaskEntity taskEntity = getAndValidateTask(bidEntity.taskId(), customerId);
        UserEntity customer = getAndValidateCustomerBalance(customerId, taskEntity.reward());

        BidEntity updatedBid = executeAcceptanceTransaction(bidEntity, taskEntity, customer);

        return mapBidEntityToResponse(updatedBid);
    }

    private BidEntity getAndValidateBid(BigInteger bidId) {
        BidEntity bidEntity = bidRepository.getBidById(bidId)
            .orElseThrow(() -> new EntityNotFoundException("Bid", bidId));

        if (!bidEntity.status().equals(BidStatusEnum.PENDING.getValue())) {
            throw new BadRequestException("Bid status must be PENDING, current status: " +
                                          BidStatusEnum.fromValue(bidEntity.status()));
        }

        return bidEntity;
    }

    private TaskEntity getAndValidateTask(BigInteger taskId, BigInteger customerId) {
        TaskEntity taskEntity = taskRepository.getTaskById(taskId)
            .orElseThrow(() -> new InternalErrorException("Task not found for ID: " + taskId));

        if (!taskEntity.customerId().equals(customerId)) {
            throw new ForbiddenException("You are not the customer of this bid");
        }

        if (!taskEntity.status().equals(TaskStatusEnum.PUBLISHED.getValue())) {
            throw new BadRequestException("Task status must be PUBLISHED, current status: " +
                                          TaskStatusEnum.fromValue(taskEntity.status()));
        }

        return taskEntity;
    }

    private UserEntity getAndValidateCustomerBalance(BigInteger executorId, BigInteger requiredReward) {
        UserEntity executor = userRepository.getUserById(executorId)
            .orElseThrow(() -> new InternalErrorException("Executor not found for ID: " + executorId));

        if (executor.balance().compareTo(requiredReward) < 0) {
            throw new BadRequestException(
                String.format(
                    "Insufficient balance. Required: %d, Available: %d",
                    requiredReward, executor.balance()
                )
            );
        }

        return executor;
    }

    private BidEntity executeAcceptanceTransaction(BidEntity bidEntity, TaskEntity taskEntity, UserEntity customer) {
        taskRepository.updateStatus(taskEntity.id(), TaskStatusEnum.IN_PROGRESS.getValue());
        taskRepository.updateExecutor(taskEntity.id(), bidEntity.executorId());

        bidRepository.updateStatusByTaskId(taskEntity.id(), BidStatusEnum.REJECTED.getValue());
        BidEntity updatedBid = bidRepository.updateStatus(bidEntity.id(), BidStatusEnum.ACCEPTED.getValue());

        userRepository.updateBalance(customer.id(), customer.balance().subtract(taskEntity.reward()));
        paymentRepository.createPayment(buildHoldPaymentEntity(taskEntity));

        log.info(
            "Bid {} accepted for task {}. Amount {} held from customer {}",
            bidEntity.id(), taskEntity.id(), taskEntity.reward(), customer.id()
        );

        return updatedBid;
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
