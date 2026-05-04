package org.soup.ssu.bench.repository;

import lombok.RequiredArgsConstructor;
import org.soup.ssu.bench.repository.entity.BidEntity;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import static org.soup.ssu.bench.constant.CommonDataConstants.ID_COL;
import static org.soup.ssu.bench.constant.CommonDataConstants.LIMIT_PARAM;
import static org.soup.ssu.bench.constant.CommonDataConstants.OFFSET_PARAM;
import static org.soup.ssu.bench.constant.CommonDataConstants.STATUS_COL;
import static org.soup.ssu.bench.repository.entity.BidEntity.BID_ROW_MAPPER;
import static org.soup.ssu.bench.repository.entity.BidEntity.TASK_ID_COL;

@Repository
@RequiredArgsConstructor
public class BidRepository {

    private static final String SQL_CREATE_BID = """
            INSERT INTO bids (status, task_id, created_at, updated_at)
            VALUES(:status, :task_id, now(), now())
            RETURNING id, status, task_id, executor_id;
        """;

    private static final String SQL_UPDATE_STATUS = """
            UPDATE bids SET status=:status, updated_at=now()
            WHERE id=:id
            RETURNING id, status, task_id, executor_id;
        """;

    private static final String SQL_GET_BID = """
            SELECT id, status, task_id, executor_id
            FROM bids
            WHERE id = :id
        """;

    private static final String SQL_GET_BIDS = """
            SELECT id, status, task_id, executor_id
            FROM bids
            ORDER BY created_at DESC LIMIT :limit OFFSET :offset
        """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public BidEntity createBid(BidEntity bidEntity) {
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue(STATUS_COL, bidEntity.status())
            .addValue(TASK_ID_COL, bidEntity.taskId());

        return jdbcTemplate.queryForObject(SQL_CREATE_BID, params, BID_ROW_MAPPER);
    }

    public BidEntity updateStatus(BigInteger id, String status) {
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue(ID_COL, id)
            .addValue(STATUS_COL, status);

        return jdbcTemplate.queryForObject(SQL_UPDATE_STATUS, params, BID_ROW_MAPPER);
    }

    public Optional<BidEntity> getBidById(BigInteger id) {
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue(ID_COL, id);

        return jdbcTemplate.query(SQL_GET_BID, params, BID_ROW_MAPPER).stream()
            .findFirst();
    }

    public List<BidEntity> getBids(int page, int size) {
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue(LIMIT_PARAM, size)
            .addValue(OFFSET_PARAM, page * size);

        return jdbcTemplate.query(SQL_GET_BIDS, params, BID_ROW_MAPPER);
    }
}
