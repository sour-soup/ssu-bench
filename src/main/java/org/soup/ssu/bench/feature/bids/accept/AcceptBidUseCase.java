package org.soup.ssu.bench.feature.bids.accept;

import lombok.RequiredArgsConstructor;
import org.soup.ssu.bench.exception.BadRequestException;
import org.soup.ssu.bench.exception.EntityNotFoundException;
import org.soup.ssu.bench.exception.ForbiddenException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ssu.bench.model.BidResponse;
import ssu.bench.model.BidStatusEnum;

import java.math.BigInteger;

@Component
@RequiredArgsConstructor
public class AcceptBidUseCase {

    private final AcceptBidRepository bidRepository;
    private final AcceptTaskRepository taskRepository;

    @Transactional
    public BidResponse execute(BigInteger bidId, BigInteger customerId) {
        BidData bidData = bidRepository.getBidData(bidId)
            .orElseThrow(() -> new EntityNotFoundException("Bid", bidId));

        if (!customerId.equals(bidData.customerId())) {
            throw new ForbiddenException("Only the customer can accept this bid");
        }

        if (!taskRepository.isPublishedTask(bidData.taskId())) {
            throw new BadRequestException("Task is not published");
        }

        bidRepository.updateBidStatusToAccepted(bidId);
        bidRepository.rejectOtherBids(bidData.taskId(), bidId);
        taskRepository.updateTaskStatusToInProgress(bidData.taskId(), bidData.executorId());

        return buildAcceptedBidResponse(bidData);
    }

    private BidResponse buildAcceptedBidResponse(BidData bidData) {
        return new BidResponse()
            .id(bidData.id())
            .status(BidStatusEnum.ACCEPTED)
            .taskId(bidData.taskId())
            .executorId(bidData.executorId())
            .createdAt(bidData.createdAt());
    }
}
