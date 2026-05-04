package org.soup.ssu.bench.repository;

import org.junit.jupiter.api.Test;
import org.soup.ssu.bench.RepositoryTest;
import org.soup.ssu.bench.repository.entity.BidEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.soup.ssu.bench.generator.EntityGenerator.buildBidEntity;

@Import(BidRepository.class)
public class BidRepositoryTest extends RepositoryTest {

    @Autowired
    private BidRepository bidRepository;

    @Test
    void givenBid_whenCreateBid_thenReturnBidWithId() {
        // given
        BidEntity bidEntity = buildBidEntity();

        // when
        BidEntity insertedBid = bidRepository.createBid(bidEntity);

        // then
        assertNotNull(insertedBid.id());
        assertEqualsBid(bidEntity.withId(insertedBid.id()), insertedBid);
    }

    @Test
    void givenBidInDb_whenUpdateStatus_thenReturnUpdatedBid() {
        // given
        String newStatus = "accepted";
        BidEntity bidEntity = bidRepository.createBid(buildBidEntity());

        // when
        BidEntity insertedBid = bidRepository.updateStatus(bidEntity.id(), newStatus);

        // then
        assertEqualsBid(bidEntity.withStatus(newStatus), insertedBid);
    }

    @Test
    void givenBidInDb_whenGetBidById_thenReturnBid() {
        // given
        BidEntity bidEntity = bidRepository.createBid(buildBidEntity());

        // when
        Optional<BidEntity> result = bidRepository.getBidById(bidEntity.id());

        // then
        assertTrue(result.isPresent());
        assertEqualsBid(bidEntity, result.get());
    }

    @Test
    void givenEmptyDb_whenGetBidById_thenReturnEmpty() {
        // when
        Optional<BidEntity> result = bidRepository.getBidById(BigInteger.valueOf(999));

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void givenBidInDb_whenGetBids_thenReturnBid() {
        // given
        BidEntity bidEntity = bidRepository.createBid(buildBidEntity());

        // when
        List<BidEntity> result = bidRepository.getBids(0, 10);

        // then
        assertThat(result).containsExactly(bidEntity);
    }

    @Test
    void givenBidsInDb_whenGetBids_thenReturnLimitBids() {
        // given
        bidRepository.createBid(buildBidEntity().withTaskId(BigInteger.valueOf(1)));
        bidRepository.createBid(buildBidEntity().withTaskId(BigInteger.valueOf(2)));
        bidRepository.createBid(buildBidEntity().withTaskId(BigInteger.valueOf(3)));

        // when
        List<BidEntity> result = bidRepository.getBids(0, 2);

        // then
        assertThat(result).hasSize(2);
    }

    @Test
    void givenBidsInDb_whenGetBids_thenReturnOffsetBids() {
        // given
        bidRepository.createBid(buildBidEntity().withTaskId(BigInteger.valueOf(1)));
        bidRepository.createBid(buildBidEntity().withTaskId(BigInteger.valueOf(2)));
        bidRepository.createBid(buildBidEntity().withTaskId(BigInteger.valueOf(3)));

        // when
        List<BidEntity> result = bidRepository.getBids(1, 2);

        // then
        assertThat(result).hasSize(1);
    }

    @Test
    void givenEmptyDb_whenGetBids_thenReturnEmptyList() {
        // when
        List<BidEntity> result = bidRepository.getBids(0, 1);

        // then
        assertThat(result).isEmpty();
    }

    private static void assertEqualsBid(BidEntity expected, BidEntity actual) {
        assertEquals(expected.id(), actual.id());
        assertEquals(expected.taskId(), actual.taskId());
        assertEquals(expected.executorId(), actual.executorId());
        assertEquals(expected.status(), actual.status());
    }
}
