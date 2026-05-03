package org.soup.ssu.bench.feature.bids.accept;

import lombok.Builder;
import lombok.With;
import ssu.bench.model.BidStatusEnum;

import java.math.BigInteger;
import java.time.LocalDateTime;

@With
@Builder
public record BidData(BigInteger id, BigInteger taskId, BigInteger executorId,
                      BidStatusEnum status, LocalDateTime createdAt, BigInteger customerId) {
}
