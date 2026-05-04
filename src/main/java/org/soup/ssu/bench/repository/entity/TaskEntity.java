package org.soup.ssu.bench.repository.entity;

import lombok.Builder;
import lombok.With;
import org.springframework.jdbc.core.RowMapper;

import java.math.BigInteger;
import java.time.LocalDateTime;

import static org.soup.ssu.bench.constant.CommonDataConstants.CREATED_AT_COL;
import static org.soup.ssu.bench.constant.CommonDataConstants.ID_COL;
import static org.soup.ssu.bench.constant.CommonDataConstants.STATUS_COL;
import static org.soup.ssu.bench.constant.CommonDataConstants.UPDATED_AT_COL;

@With
@Builder
public record TaskEntity(BigInteger id,
                         String title,
                         String description,
                         BigInteger reward,
                         String status,
                         BigInteger customerId,
                         BigInteger executorId,
                         LocalDateTime createdAt,
                         LocalDateTime updatedAt) {

    public static final String TITLE_COL = "title";
    public static final String DESCRIPTION_COL = "description";
    public static final String REWARD_COL = "reward";
    public static final String CUSTOMER_ID_COL = "customer_id";
    public static final String EXECUTOR_ID_COL = "executor_id";

    public static final RowMapper<TaskEntity> TASK_ROW_MAPPER = (rs, rowNum) -> TaskEntity.builder()
        .id(BigInteger.valueOf(rs.getLong(ID_COL)))
        .title(rs.getString(TITLE_COL))
        .description(rs.getString(DESCRIPTION_COL))
        .reward(BigInteger.valueOf(rs.getLong(REWARD_COL)))
        .status(rs.getString(STATUS_COL))
        .customerId(rs.getObject(CUSTOMER_ID_COL, BigInteger.class))
        .executorId(rs.getObject(EXECUTOR_ID_COL, BigInteger.class))
        .createdAt(rs.getObject(CREATED_AT_COL, LocalDateTime.class))
        .updatedAt(rs.getObject(UPDATED_AT_COL, LocalDateTime.class))
        .build();
}
