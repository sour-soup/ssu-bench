package org.soup.ssu.bench.repository;

import lombok.RequiredArgsConstructor;
import org.soup.ssu.bench.repository.entity.TaskEntity;
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
import static org.soup.ssu.bench.repository.entity.TaskEntity.CUSTOMER_ID_COL;
import static org.soup.ssu.bench.repository.entity.TaskEntity.DESCRIPTION_COL;
import static org.soup.ssu.bench.repository.entity.TaskEntity.EXECUTOR_ID_COL;
import static org.soup.ssu.bench.repository.entity.TaskEntity.REWARD_COL;
import static org.soup.ssu.bench.repository.entity.TaskEntity.TASK_ROW_MAPPER;
import static org.soup.ssu.bench.repository.entity.TaskEntity.TITLE_COL;

@Repository
@RequiredArgsConstructor
public class TaskRepository {

    private static final String SQL_CREATE_TASK = """
            INSERT INTO tasks (title, description, reward, status, customer_id, created_at, updated_at)
            VALUES(:title, :description, :reward, :status, :customer_id, now(), now())
            RETURNING id, title, description, reward, status, customer_id, executor_id, created_at, updated_at;
        """;

    private static final String SQL_UPDATE_EXECUTOR = """
            UPDATE tasks SET executor_id=:executor_id, updated_at=now()
            WHERE id=:id
            RETURNING id, title, description, reward, status, customer_id, executor_id, created_at, updated_at;
        """;

    private static final String SQL_UPDATE_STATUS = """
            UPDATE tasks SET status=:status, updated_at=now()
            WHERE id=:id
            RETURNING id, title, description, reward, status, customer_id, executor_id, created_at, updated_at;
        """;

    private static final String SQL_GET_TASK = """
            SELECT id, title, description, reward, status, customer_id, executor_id, created_at, updated_at
            FROM tasks
            WHERE id = :id
        """;

    private static final String SQL_GET_TASKS = """
            SELECT id, title, description, reward, status, customer_id, executor_id, created_at, updated_at
            FROM tasks
            ORDER BY created_at DESC LIMIT :limit OFFSET :offset
        """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public TaskEntity createTask(TaskEntity taskEntity) {
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue(TITLE_COL, taskEntity.title())
            .addValue(DESCRIPTION_COL, taskEntity.description())
            .addValue(REWARD_COL, taskEntity.reward())
            .addValue(STATUS_COL, taskEntity.status())
            .addValue(CUSTOMER_ID_COL, taskEntity.customerId());

        return jdbcTemplate.queryForObject(SQL_CREATE_TASK, params, TASK_ROW_MAPPER);
    }

    public TaskEntity updateExecutor(BigInteger id, BigInteger executorId) {
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue(ID_COL, id)
            .addValue(EXECUTOR_ID_COL, executorId);

        return jdbcTemplate.queryForObject(SQL_UPDATE_EXECUTOR, params, TASK_ROW_MAPPER);
    }

    public TaskEntity updateStatus(BigInteger id, String status) {
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue(ID_COL, id)
            .addValue(STATUS_COL, status);

        return jdbcTemplate.queryForObject(SQL_UPDATE_STATUS, params, TASK_ROW_MAPPER);
    }

    public Optional<TaskEntity> getTaskById(BigInteger id) {
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue(ID_COL, id);

        return jdbcTemplate.query(SQL_GET_TASK, params, TASK_ROW_MAPPER).stream()
            .findFirst();
    }

    public List<TaskEntity> getTasks(int page, int size) {
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue(LIMIT_PARAM, size)
            .addValue(OFFSET_PARAM, page * size);

        return jdbcTemplate.query(SQL_GET_TASKS, params, TASK_ROW_MAPPER);
    }
}
