package org.soup.ssu.bench.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import ssu.bench.model.BidResponse;
import ssu.bench.model.BidStatusEnum;
import ssu.bench.model.PageBidResponse;
import ssu.bench.model.TaskStatusEnum;

import java.math.BigInteger;
import java.time.LocalDateTime;
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

@ExtendWith(MockitoExtension.class)
class BidServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private BidRepository bidRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BidService bidService;

    private static final BigInteger BID_ID = BigInteger.valueOf(1);
    private static final BigInteger TASK_ID = BigInteger.valueOf(100);
    private static final BigInteger EXECUTOR_ID = BigInteger.valueOf(200);
    private static final BigInteger CUSTOMER_ID = BigInteger.valueOf(300);
    private static final BigInteger DIFFERENT_CUSTOMER_ID = BigInteger.valueOf(999);
    private static final BigInteger REWARD = BigInteger.valueOf(1000);

    @Test
    void givenExistingBid_whenGetBidById_thenReturnBid() {
        // given
        BidEntity bidEntity = buildBidEntity().withId(BID_ID);
        when(bidRepository.getBidById(BID_ID)).thenReturn(Optional.of(bidEntity));

        // when
        BidResponse response = bidService.getBidById(BID_ID);

        // then
        assertBidMapping(bidEntity, response);
        verify(bidRepository).getBidById(BID_ID);
    }

    @Test
    void givenNonExistingBid_whenGetBidById_thenThrowEntityNotFoundException() {
        // given
        when(bidRepository.getBidById(BID_ID)).thenReturn(Optional.empty());

        // when & then
        assertThrows(EntityNotFoundException.class, () -> bidService.getBidById(BID_ID));
        verify(bidRepository).getBidById(BID_ID);
    }

    @Test
    void givenBidsExist_whenGetTaskBids_thenReturnPageBidResponse() {
        // given
        int page = 0;
        int size = 10;

        BidEntity bidEntity1 = buildBidEntity().withId(BigInteger.ONE);
        BidEntity bidEntity2 = buildBidEntity().withId(BigInteger.TWO);
        List<BidEntity> bidEntities = List.of(bidEntity1, bidEntity2);

        when(bidRepository.getBids(TASK_ID, page, size)).thenReturn(bidEntities);

        // when
        PageBidResponse response = bidService.getTaskBids(TASK_ID, page, size);

        // then
        assertNotNull(response);
        assertEquals(page, response.getPage());
        assertEquals(size, response.getSize());
        assertEquals(2, response.getContent().size());
    }

    @Test
    void givenNoBids_whenGetTaskBids_thenReturnEmptyPageBidResponse() {
        // given
        int page = 0;
        int size = 10;

        when(bidRepository.getBids(TASK_ID, page, size)).thenReturn(List.of());

        // when
        PageBidResponse response = bidService.getTaskBids(TASK_ID, page, size);

        // then
        assertNotNull(response);
        assertEquals(0, response.getContent().size());
    }

    @Test
    void givenValidRequest_whenCreateBid_thenReturnCreatedBid() {
        // given
        BidEntity bidEntity = buildBidEntity();
        BidEntity savedBidEntity = bidEntity.withId(BID_ID);

        when(bidRepository.createBid(any(BidEntity.class))).thenReturn(savedBidEntity);

        // when
        BidResponse response = bidService.createBid(TASK_ID, EXECUTOR_ID);

        // then
        assertNotNull(response);
        assertEquals(BID_ID, response.getId());
        assertEquals(TASK_ID, response.getTaskId());
        assertEquals(EXECUTOR_ID, response.getExecutorId());
        assertEquals(BidStatusEnum.PENDING, response.getStatus());
        assertNotNull(response.getCreatedAt());
    }

    @Test
    void givenValidBid_whenAcceptBid_thenAcceptAndProcessPayment() {
        // given
        BidEntity bidEntity = buildBidEntity()
            .withId(BID_ID)
            .withTaskId(TASK_ID)
            .withExecutorId(EXECUTOR_ID)
            .withStatus(BidStatusEnum.PENDING.getValue());

        TaskEntity taskEntity = buildTaskEntity()
            .withId(TASK_ID)
            .withCustomerId(CUSTOMER_ID)
            .withReward(REWARD)
            .withStatus(TaskStatusEnum.PUBLISHED.getValue());

        UserEntity customer = buildUserEntity()
            .withId(CUSTOMER_ID)
            .withBalance(BigInteger.valueOf(5000));

        when(bidRepository.getBidById(BID_ID)).thenReturn(Optional.of(bidEntity));
        when(taskRepository.getTaskById(TASK_ID)).thenReturn(Optional.of(taskEntity));
        when(userRepository.getUserById(CUSTOMER_ID)).thenReturn(Optional.of(customer));
        when(bidRepository.updateStatus(BID_ID, BidStatusEnum.ACCEPTED.getValue()))
            .thenReturn(bidEntity.withStatus(BidStatusEnum.ACCEPTED.getValue()));

        // when
        BidResponse response = bidService.acceptBid(BID_ID, CUSTOMER_ID);

        // then
        assertBidMapping(bidEntity.withStatus(BidStatusEnum.ACCEPTED.getValue()), response);

        verify(userRepository).updateBalance(CUSTOMER_ID, customer.balance().subtract(taskEntity.reward()));
        verify(paymentRepository).createPayment(any(PaymentEntity.class));
        verify(taskRepository).updateStatus(TASK_ID, TaskStatusEnum.IN_PROGRESS.getValue());
        verify(taskRepository).updateExecutor(TASK_ID, EXECUTOR_ID);
        verify(bidRepository).updateStatusByTaskId(TASK_ID, BidStatusEnum.REJECTED.getValue());
        verify(bidRepository).updateStatus(BID_ID, BidStatusEnum.ACCEPTED.getValue());
    }

    @Test
    void givenNonExistingBid_whenAcceptBid_thenThrowEntityNotFoundException() {
        // given
        when(bidRepository.getBidById(BID_ID)).thenReturn(Optional.empty());

        // when & then
        assertThrows(EntityNotFoundException.class, () -> bidService.acceptBid(BID_ID, CUSTOMER_ID));
        verifyNoInteractions(taskRepository, userRepository, paymentRepository);
    }

    @Test
    void givenNonPendingBid_whenAcceptBid_thenThrowBadRequestException() {
        // given
        BidEntity bidEntity = buildBidEntity()
            .withId(BID_ID)
            .withExecutorId(EXECUTOR_ID)
            .withStatus(BidStatusEnum.ACCEPTED.getValue());

        when(bidRepository.getBidById(BID_ID)).thenReturn(Optional.of(bidEntity));

        // when & then
        assertThrows(BadRequestException.class, () -> bidService.acceptBid(BID_ID, CUSTOMER_ID));
        verify(taskRepository, never()).getTaskById(any());
    }

    @Test
    void givenTaskNotFound_whenAcceptBid_thenThrowInternalErrorException() {
        // given
        BidEntity bidEntity = buildBidEntity()
            .withId(BID_ID)
            .withTaskId(TASK_ID)
            .withExecutorId(EXECUTOR_ID)
            .withStatus(BidStatusEnum.PENDING.getValue());

        when(bidRepository.getBidById(BID_ID)).thenReturn(Optional.of(bidEntity));
        when(taskRepository.getTaskById(TASK_ID)).thenReturn(Optional.empty());

        // when & then
        assertThrows(InternalErrorException.class, () -> bidService.acceptBid(BID_ID, CUSTOMER_ID));
    }

    @Test
    void givenDifferentCustomer_whenAcceptBid_thenThrowForbiddenException() {
        // given
        BidEntity bidEntity = buildBidEntity()
            .withId(BID_ID)
            .withTaskId(TASK_ID)
            .withExecutorId(EXECUTOR_ID)
            .withStatus(BidStatusEnum.PENDING.getValue());

        TaskEntity taskEntity = buildTaskEntity()
            .withId(TASK_ID)
            .withCustomerId(CUSTOMER_ID)
            .withStatus(TaskStatusEnum.PUBLISHED.getValue());

        when(bidRepository.getBidById(BID_ID)).thenReturn(Optional.of(bidEntity));
        when(taskRepository.getTaskById(TASK_ID)).thenReturn(Optional.of(taskEntity));

        // when & then
        assertThrows(ForbiddenException.class, () -> bidService.acceptBid(BID_ID, DIFFERENT_CUSTOMER_ID));
        verify(userRepository, never()).getUserById(any());
    }

    @Test
    void givenNonPublishedTask_whenAcceptBid_thenThrowBadRequestException() {
        // given
        BidEntity bidEntity = buildBidEntity()
            .withId(BID_ID)
            .withTaskId(TASK_ID)
            .withExecutorId(EXECUTOR_ID)
            .withStatus(BidStatusEnum.PENDING.getValue());

        TaskEntity taskEntity = buildTaskEntity()
            .withId(TASK_ID)
            .withCustomerId(CUSTOMER_ID)
            .withStatus(TaskStatusEnum.IN_PROGRESS.getValue());

        when(bidRepository.getBidById(BID_ID)).thenReturn(Optional.of(bidEntity));
        when(taskRepository.getTaskById(TASK_ID)).thenReturn(Optional.of(taskEntity));

        // when & then
        assertThrows(BadRequestException.class, () -> bidService.acceptBid(BID_ID, CUSTOMER_ID));
        verify(userRepository, never()).getUserById(any());
    }

    @Test
    void givenExecutorNotFound_whenAcceptBid_thenThrowInternalErrorException() {
        // given
        BidEntity bidEntity = buildBidEntity()
            .withId(BID_ID)
            .withTaskId(TASK_ID)
            .withExecutorId(EXECUTOR_ID)
            .withStatus(BidStatusEnum.PENDING.getValue());

        TaskEntity taskEntity = buildTaskEntity()
            .withId(TASK_ID)
            .withCustomerId(CUSTOMER_ID)
            .withStatus(TaskStatusEnum.PUBLISHED.getValue())
            .withReward(REWARD);

        when(bidRepository.getBidById(BID_ID)).thenReturn(Optional.of(bidEntity));
        when(taskRepository.getTaskById(TASK_ID)).thenReturn(Optional.of(taskEntity));
        when(userRepository.getUserById(CUSTOMER_ID)).thenReturn(Optional.empty());

        // when & then
        assertThrows(InternalErrorException.class, () -> bidService.acceptBid(BID_ID, CUSTOMER_ID));
    }

    @Test
    void givenInsufficientBalance_whenAcceptBid_thenThrowBadRequestException() {
        // given
        BidEntity bidEntity = buildBidEntity()
            .withId(BID_ID)
            .withTaskId(TASK_ID)
            .withExecutorId(EXECUTOR_ID)
            .withStatus(BidStatusEnum.PENDING.getValue());

        TaskEntity taskEntity = buildTaskEntity()
            .withId(TASK_ID)
            .withCustomerId(CUSTOMER_ID)
            .withStatus(TaskStatusEnum.PUBLISHED.getValue())
            .withReward(REWARD);

        UserEntity customer = buildUserEntity()
            .withId(CUSTOMER_ID)
            .withBalance(BigInteger.valueOf(500));

        when(bidRepository.getBidById(BID_ID)).thenReturn(Optional.of(bidEntity));
        when(taskRepository.getTaskById(TASK_ID)).thenReturn(Optional.of(taskEntity));
        when(userRepository.getUserById(CUSTOMER_ID)).thenReturn(Optional.of(customer));

        // when & then
        assertThrows(BadRequestException.class, () -> bidService.acceptBid(BID_ID, CUSTOMER_ID));
        verify(userRepository, never()).updateBalance(any(), any());
        verify(paymentRepository, never()).createPayment(any());
    }

    private static void assertBidMapping(BidEntity bidEntity, BidResponse response) {
        assertEquals(bidEntity.id(), response.getId());
        assertEquals(bidEntity.taskId(), response.getTaskId());
        assertEquals(bidEntity.executorId(), response.getExecutorId());
        assertEquals(bidEntity.status(), response.getStatus().getValue());
        assertEquals(bidEntity.createdAt(), response.getCreatedAt());
    }

    private BidEntity buildBidEntity() {
        return BidEntity.builder()
            .taskId(TASK_ID)
            .executorId(EXECUTOR_ID)
            .status(BidStatusEnum.PENDING.getValue())
            .createdAt(LocalDateTime.now())
            .build();
    }

    private TaskEntity buildTaskEntity() {
        return TaskEntity.builder()
            .id(TASK_ID)
            .customerId(CUSTOMER_ID)
            .executorId(EXECUTOR_ID)
            .reward(REWARD)
            .status(TaskStatusEnum.PUBLISHED.getValue())
            .title("Test Task")
            .description("Test Description")
            .build();
    }

    private UserEntity buildUserEntity() {
        return UserEntity.builder()
            .id(EXECUTOR_ID)
            .balance(BigInteger.valueOf(5000))
            .build();
    }
}
