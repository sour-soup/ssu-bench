package org.soup.ssu.bench.feature.bids.accept;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soup.ssu.bench.RepositoryTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import ssu.bench.model.TaskStatusEnum;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import(AcceptTaskRepository.class)
public class AcceptTaskRepositoryTest extends RepositoryTest {

    @Autowired
    private AcceptTaskRepository acceptTaskRepository;
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    private static final BigInteger CUSTOMER_ID = BigInteger.ONE;
    private static final BigInteger EXECUTOR_ID = BigInteger.TWO;

    @Test
    @DisplayName("updateTaskStatusToInProgress обновляет статус задачи")
    void shouldUpdateTaskStatusToInProgress() {
        // Given
        BigInteger taskId = insertTestTask(TaskStatusEnum.PUBLISHED);

        // When
        acceptTaskRepository.updateTaskStatusToInProgress(taskId, EXECUTOR_ID);

        // Then
        TaskStatusEnum status = getTaskStatus(taskId);
        assertThat(status).isEqualTo(TaskStatusEnum.IN_PROGRESS);
    }

    @Test
    @DisplayName("isPublishedTask возвращает true")
    void shouldReturnTrueWhenTaskIsPublished() {
        // Given
        BigInteger taskId = insertTestTask(TaskStatusEnum.PUBLISHED);

        // When
        boolean result = acceptTaskRepository.isPublishedTask(taskId);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("isPublishedTask возвращает false")
    void shouldReturnFalseWhenTaskIsInProgress() {
        // Given
        BigInteger taskId = insertTestTask(TaskStatusEnum.IN_PROGRESS);

        // When
        boolean result = acceptTaskRepository.isPublishedTask(taskId);

        // Then
        assertTrue(result);
    }

    private BigInteger insertTestTask(TaskStatusEnum status) {
        String sql = """
            INSERT INTO tasks (title, description, reward, status, customer_id, executor_id, created_at, updated_at)
            VALUES (:title, 'Description', 100, :status, :customerId, :executorId, NOW(), NOW())
            RETURNING id
            """;

        var params = new MapSqlParameterSource()
            .addValue("title", "Task")
            .addValue("status", status.name())
            .addValue("customerId", AcceptTaskRepositoryTest.CUSTOMER_ID)
            .addValue("executorId", null);

        var keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(sql, params, keyHolder);

        return BigInteger.valueOf(keyHolder.getKey().longValue());
    }

    private TaskStatusEnum getTaskStatus(BigInteger taskId) {
        String sql = "SELECT status FROM tasks WHERE id = :taskId";
        var params = new MapSqlParameterSource()
            .addValue("taskId", taskId);

        String status = jdbcTemplate.queryForObject(sql, params, String.class);
        return TaskStatusEnum.fromValue(status);
    }
}
