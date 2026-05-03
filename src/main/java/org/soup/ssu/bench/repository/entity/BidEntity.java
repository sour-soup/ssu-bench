package org.soup.ssu.bench.repository.entity;

import lombok.Builder;
import lombok.With;
import org.springframework.jdbc.core.RowMapper;

import java.math.BigInteger;

import static org.soup.ssu.bench.constant.CommonDataConstants.ID_COL;
import static org.soup.ssu.bench.constant.CommonDataConstants.STATUS_COL;

@With
@Builder
public record BidEntity(BigInteger id,
                        String status,
                        BigInteger taskId,
                        BigInteger executorId) {

    public static final String TASK_ID_COL = "task_id";
    public static final String EXECUTOR_ID_COL = "executor_id";

    public static final RowMapper<BidEntity> BID_ROW_MAPPER = (rs, rowNum) -> BidEntity.builder()
        .id(BigInteger.valueOf(rs.getLong(ID_COL)))
        .status(rs.getString(STATUS_COL))
        .taskId(rs.getObject(TASK_ID_COL, BigInteger.class))
        .executorId(rs.getObject(EXECUTOR_ID_COL, BigInteger.class))
        .build();
}
