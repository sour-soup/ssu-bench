package org.soup.ssu.bench.feature.bids.accept;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ssu.bench.model.TaskStatusEnum;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Repository
@RequiredArgsConstructor
public class AcceptTaskRepository {

    static final String PARAM_STATUS = "status";
    static final String PARAM_UPDATED_AT = "updatedAt";
    static final String PARAM_TASK_ID = "taskId";
    static final String PARAM_EXECUTOR_ID = "executorId";

    private static final String SQL_SELECT_TASK = """
            SELECT COUNT(*) FROM tasks
            WHERE id = :taskId AND status = :status
        """;

    private static final String SQL_UPDATE_TASK_STATUS = """
            UPDATE tasks SET status = :status, executor_id = :executorId, updated_at = :updatedAt
            WHERE id = :taskId
        """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public boolean isPublishedTask(BigInteger taskId) {
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue(PARAM_TASK_ID, taskId)
            .addValue(PARAM_STATUS, TaskStatusEnum.PUBLISHED.name());

        Integer count = jdbcTemplate.queryForObject(SQL_SELECT_TASK, params, Integer.class);
        return count != null && count > 0;
    }

    public void updateTaskStatusToInProgress(BigInteger taskId, BigInteger executorId) {
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue(PARAM_TASK_ID, taskId)
            .addValue(PARAM_STATUS, TaskStatusEnum.IN_PROGRESS.name())
            .addValue(PARAM_EXECUTOR_ID, executorId)
            .addValue(PARAM_UPDATED_AT, Timestamp.valueOf(LocalDateTime.now()));

        jdbcTemplate.update(SQL_UPDATE_TASK_STATUS, params);
    }
}
