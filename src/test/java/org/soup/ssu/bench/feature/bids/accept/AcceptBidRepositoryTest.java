package org.soup.ssu.bench.feature.bids.accept;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soup.ssu.bench.RepositoryTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import ssu.bench.model.BidStatusEnum;
import ssu.bench.model.TaskStatusEnum;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import(AcceptBidRepository.class)
class AcceptBidRepositoryTest extends RepositoryTest {

    @Autowired
    private AcceptBidRepository acceptBidRepository;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    private BigInteger bidId;
    private BigInteger taskId;
    private BigInteger executorId;
    private BigInteger customerId;

    @BeforeEach
    void setUp() {
        customerId = insertTestUser("customer", "CUSTOMER");
        executorId = insertTestUser("executor", "EXECUTOR");
        taskId = insertTestTask("Published Task", TaskStatusEnum.PUBLISHED, customerId, null);
        bidId = insertBid(taskId, executorId, BidStatusEnum.PENDING);
    }

    @Test
    @DisplayName("getBidData возвращает данные заявки")
    void shouldGetBidData() {
        // When
        BidData bidData = acceptBidRepository.getBidData(bidId)
            .orElseThrow();

        // Then
        assertThat(bidData).isNotNull();
        assertThat(bidData.id()).isEqualTo(bidId);
        assertThat(bidData.taskId()).isEqualTo(taskId);
        assertThat(bidData.executorId()).isEqualTo(executorId);
        assertThat(bidData.customerId()).isEqualTo(customerId);
    }

    @Test
    @DisplayName("getBidData бросает EntityNotFoundException когда заявка не найдена")
    void shouldThrowEntityNotFoundExceptionWhenBidNotFound() {
        // Given
        BigInteger nonExistentBidId = BigInteger.valueOf(999L);

        // When & Then
        assertThatThrownBy(() -> acceptBidRepository.getBidData(nonExistentBidId))
            .isInstanceOf(org.soup.ssu.bench.exception.EntityNotFoundException.class)
            .hasMessageContaining("Bid");
    }

    @Test
    @DisplayName("updateBidStatusToAccepted обновляет статус заявки")
    void shouldUpdateBidStatusToAccepted() {
        // When
        acceptBidRepository.updateBidStatusToAccepted(bidId);

        // Then
        BidStatusEnum status = getBidStatus(bidId);
        assertThat(status).isEqualTo(BidStatusEnum.ACCEPTED);
    }

    @Test
    @DisplayName("rejectOtherBids отклоняет другие заявки")
    void shouldRejectOtherBids() {
        // Given
        BigInteger otherExecutorId = insertTestUser("other_executor", "EXECUTOR");
        BigInteger otherBidId = insertBid(taskId, otherExecutorId, BidStatusEnum.PENDING);

        // When
        acceptBidRepository.rejectOtherBids(taskId, bidId);

        // Then
        BidStatusEnum myStatus = getBidStatus(bidId);
        BidStatusEnum otherStatus = getBidStatus(otherBidId);
        assertThat(myStatus).isEqualTo(BidStatusEnum.PENDING);
        assertThat(otherStatus).isEqualTo(BidStatusEnum.REJECTED);
    }

    private BigInteger insertTestUser(String username, String role) {
        String sql = """
            INSERT INTO users (username, password_hash, role, balance, status, created_at, updated_at)
            VALUES (:username, 'hash', :role, 0, 'ACTIVE', NOW(), NOW())
            RETURNING id
            """;

        var params = new MapSqlParameterSource()
            .addValue("username", username)
            .addValue("role", role);

        var keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(sql, params, keyHolder);

        return BigInteger.valueOf(keyHolder.getKey().longValue());
    }

    private BigInteger insertTestTask(String title, TaskStatusEnum status, BigInteger customerId, BigInteger executorId) {
        String sql = """
            INSERT INTO tasks (title, description, reward, status, customer_id, executor_id, created_at, updated_at)
            VALUES (:title, 'Description', 100, :status, :customerId, :executorId, NOW(), NOW())
            RETURNING id
            """;

        var params = new MapSqlParameterSource()
            .addValue("title", title)
            .addValue("status", status.name())
            .addValue("customerId", customerId)
            .addValue("executorId", executorId);

        var keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(sql, params, keyHolder);

        return BigInteger.valueOf(keyHolder.getKey().longValue());
    }

    private BigInteger insertBid(BigInteger taskId, BigInteger executorId, BidStatusEnum status) {
        String sql = """
            INSERT INTO bids (task_id, executor_id, status, created_at, updated_at)
            VALUES (:taskId, :executorId, :status, NOW(), NOW())
            RETURNING id
            """;

        var params = new MapSqlParameterSource()
            .addValue("taskId", taskId)
            .addValue("executorId", executorId)
            .addValue("status", status.name());

        var keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(sql, params, keyHolder);

        return BigInteger.valueOf(keyHolder.getKey().longValue());
    }

    private BidStatusEnum getBidStatus(BigInteger bidId) {
        String sql = "SELECT status FROM bids WHERE id = :bidId";
        var params = new MapSqlParameterSource()
            .addValue("bidId", bidId);

        String status = jdbcTemplate.queryForObject(sql, params, String.class);
        return BidStatusEnum.fromValue(status);
    }
}
