package org.soup.ssu.bench.feature.bids.accept;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.soup.ssu.bench.exception.BadRequestException;
import org.soup.ssu.bench.exception.EntityNotFoundException;
import org.soup.ssu.bench.exception.ForbiddenException;
import ssu.bench.model.BidResponse;
import ssu.bench.model.BidStatusEnum;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AcceptBidUseCaseTest {

    @Mock
    private AcceptBidRepository bidRepository;
    @Mock
    private AcceptTaskRepository taskRepository;

    @InjectMocks
    private AcceptBidUseCase acceptBidUseCase;

    private static final BigInteger BID_ID = BigInteger.ONE;
    private static final BigInteger TASK_ID = BigInteger.valueOf(10L);
    private static final BigInteger EXECUTOR_ID = BigInteger.valueOf(20L);
    private static final BigInteger CUSTOMER_ID = BigInteger.valueOf(30L);
    private static final BigInteger OTHER_CUSTOMER_ID = BigInteger.valueOf(40L);

    @Test
    @DisplayName("Успешное принятие заявки")
    void shouldAcceptBid() {
        // Given
        given(bidRepository.getBidData(BID_ID)).willReturn(Optional.of(buildBidData()));
        given(taskRepository.isPublishedTask(TASK_ID)).willReturn(true);

        // When
        BidResponse response = acceptBidUseCase.execute(BID_ID, CUSTOMER_ID);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(BidStatusEnum.ACCEPTED);
        verify(bidRepository).updateBidStatusToAccepted(BID_ID);
        verify(bidRepository).rejectOtherBids(TASK_ID, BID_ID);
        verify(taskRepository).updateTaskStatusToInProgress(TASK_ID, EXECUTOR_ID);
    }

    @Test
    @DisplayName("Бросает EntityNotFoundException когда заявка не найдена")
    void shouldThrowEntityNotFoundExceptionWhenBidNotFound() {
        // Given
        given(bidRepository.getBidData(BID_ID))
            .willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> acceptBidUseCase.execute(BID_ID, CUSTOMER_ID))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("Bid");
    }

    @Test
    @DisplayName("Бросает ForbiddenException когда пользователь не владелец задачи")
    void shouldThrowForbiddenExceptionWhenUserIsNotCustomer() {
        // Given
        given(bidRepository.getBidData(BID_ID)).willReturn(Optional.of(buildBidData()));

        // When & Then
        assertThatThrownBy(() -> acceptBidUseCase.execute(BID_ID, OTHER_CUSTOMER_ID))
            .isInstanceOf(ForbiddenException.class)
            .hasMessage("Only the customer can accept this bid");
    }

    @Test
    @DisplayName("Бросает BadRequestException когда задача не в статусе PUBLISHED")
    void shouldThrowBadRequestExceptionWhenBidAlreadyAccepted() {
        // Given
        given(bidRepository.getBidData(BID_ID)).willReturn(Optional.of(buildBidData()));
        given(taskRepository.isPublishedTask(TASK_ID)).willReturn(false);

        // When & Then
        assertThatThrownBy(() -> acceptBidUseCase.execute(BID_ID, CUSTOMER_ID))
            .isInstanceOf(BadRequestException.class)
            .hasMessage("A bid has already been accepted for this task");

        verify(bidRepository, never()).updateBidStatusToAccepted(ArgumentMatchers.any());
    }

    private static BidData buildBidData() {
        return BidData.builder()
            .id(BID_ID)
            .taskId(TASK_ID)
            .customerId(CUSTOMER_ID)
            .executorId(EXECUTOR_ID)
            .createdAt(LocalDateTime.now())
            .status(BidStatusEnum.PENDING)
            .build();
    }
}
