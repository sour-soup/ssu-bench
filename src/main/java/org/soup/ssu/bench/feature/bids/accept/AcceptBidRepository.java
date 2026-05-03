package org.soup.ssu.bench.feature.bids.accept;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ssu.bench.model.BidStatusEnum;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
class AcceptBidRepository {

    static final String PARAM_BID_ID = "bidId";
    static final String PARAM_STATUS = "status";
    static final String PARAM_UPDATED_AT = "updatedAt";
    static final String PARAM_TASK_ID = "taskId";
    static final String PARAM_PENDING_STATUS = "pendingStatus";

    private static final String SQL_GET_BID = """
            SELECT b.id, b.task_id, b.executor_id, b.status, b.created_at, t.customer_id
            FROM bids b
            JOIN tasks t ON b.task_id = t.id
            WHERE b.id = :bidId
        """;

    private static final String SQL_UPDATE_BID_STATUS = """
            UPDATE bids SET status = :status, updated_at = :updatedAt WHERE id = :bidId
        """;

    private static final String SQL_REJECT_OTHER_BIDS = """
            UPDATE bids SET status = :status, updated_at = :updatedAt
            WHERE task_id = :taskId AND id != :bidId AND status = :pendingStatus
        """;

    private static final AcceptBidRowMapper BID_DATA_ROW_MAPPER = new AcceptBidRowMapper();

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public Optional<BidData> getBidData(BigInteger bidId) {
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue(PARAM_BID_ID, bidId);

        return jdbcTemplate.query(SQL_GET_BID, params, BID_DATA_ROW_MAPPER).stream()
            .findFirst();
    }

    public void updateBidStatusToAccepted(BigInteger bidId) {
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue(PARAM_BID_ID, bidId)
            .addValue(PARAM_STATUS, BidStatusEnum.ACCEPTED.name())
            .addValue(PARAM_UPDATED_AT, Timestamp.valueOf(LocalDateTime.now()));

        jdbcTemplate.update(SQL_UPDATE_BID_STATUS, params);
    }

    public void rejectOtherBids(BigInteger taskId, BigInteger bidId) {
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue(PARAM_TASK_ID, taskId)
            .addValue(PARAM_BID_ID, bidId)
            .addValue(PARAM_STATUS, BidStatusEnum.REJECTED)
            .addValue(PARAM_PENDING_STATUS, BidStatusEnum.PENDING)
            .addValue(PARAM_UPDATED_AT, Timestamp.valueOf(LocalDateTime.now()));

        jdbcTemplate.update(SQL_REJECT_OTHER_BIDS, params);
    }
}
