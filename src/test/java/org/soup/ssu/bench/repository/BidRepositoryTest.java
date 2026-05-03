package org.soup.ssu.bench.repository;

import org.assertj.core.api.AssertionsForInterfaceTypes;
import org.junit.jupiter.api.Test;
import org.soup.ssu.bench.RepositoryTest;
import org.soup.ssu.bench.repository.entity.BidEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
        assertEquals(bidEntity.withId(insertedBid.id()), insertedBid);
    }

    @Test
    void givenBidInDb_whenUpdateStatus_thenReturnUpdatedBid() {
        // given
        String newStatus = "accepted";
        BidEntity bidEntity = bidRepository.createBid(buildBidEntity());

        // when
        BidEntity insertedBid = bidRepository.updateStatus(bidEntity.id(), newStatus);

        // then
        assertEquals(bidEntity.withStatus(newStatus), insertedBid);
    }

    @Test
    void givenBidInDb_whenGetBidById_thenReturnBid() {
        // given
        BidEntity bidEntity = bidRepository.createBid(buildBidEntity());

        // when
        Optional<BidEntity> result = bidRepository.getBidById(bidEntity.id());

        // then
        assertThat(result).get().isEqualTo(bidEntity);
    }

    @Test
    void givenEmptyDb_whenGetBidByUd_thenReturnEmpty() {
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
        AssertionsForInterfaceTypes.assertThat(result).containsExactly(bidEntity);
    }

    @Test
    void givenBidsInDb_whenGetBids_thenReturnLimitBids() {
        // given
        bidRepository.createBid(buildBidEntity());
        bidRepository.createBid(buildBidEntity());
        bidRepository.createBid(buildBidEntity());

        // when
        List<BidEntity> result = bidRepository.getBids(0, 2);

        // then
        AssertionsForInterfaceTypes.assertThat(result).hasSize(2);
    }

    @Test
    void givenBidsInDb_whenGetBids_thenReturnOffsetBids() {
        // given
        bidRepository.createBid(buildBidEntity());
        bidRepository.createBid(buildBidEntity());
        bidRepository.createBid(buildBidEntity());

        // when
        List<BidEntity> result = bidRepository.getBids(1, 2);

        // then
        AssertionsForInterfaceTypes.assertThat(result).hasSize(1);
    }

    @Test
    void givenEmptyDb_whenGetBids_thenReturnEmptyList() {
        // when
        List<BidEntity> result = bidRepository.getBids(0, 1);

        // then
        AssertionsForInterfaceTypes.assertThat(result).isEmpty();
    }

    private static BidEntity buildBidEntity() {
        return BidEntity.builder()
            .status("new")
            .taskId(BigInteger.ONE)
            .build();
    }
}
