package org.soup.ssu.bench.feature.admin.blockuser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soup.ssu.bench.RepositoryTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import ssu.bench.model.UserStatusEnum;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@Import(BlockUserRepository.class)
class BlockUserRepositoryTest extends RepositoryTest {

    @Autowired
    private BlockUserRepository blockUserRepository;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    private BigInteger userId;

    @BeforeEach
    void setUp() {
        userId = insertTestUser();
    }

    @Test
    @DisplayName("blockUser успешно блокирует пользователя")
    void shouldBlockUser() {
        // Given
        assertThat(blockUserRepository.userExists(userId)).isTrue();

        // When
        blockUserRepository.blockUser(userId);

        // Then
        UserStatusEnum status = getUserStatus(userId);
        assertThat(status).isEqualTo(UserStatusEnum.BLOCKED);
    }

    @Test
    @DisplayName("userExists возвращает true для существующего пользователя")
    void shouldReturnTrueWhenUserExists() {
        // When
        boolean exists = blockUserRepository.userExists(userId);

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("userExists возвращает false для несуществующего пользователя")
    void shouldReturnFalseWhenUserDoesNotExist() {
        // Given
        BigInteger nonExistentUserId = BigInteger.valueOf(999L);

        // When
        boolean exists = blockUserRepository.userExists(nonExistentUserId);

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("blockUser обновляет updated_at при блокировке")
    void shouldUpdateUpdatedAtWhenBlocking() {
        // Given
        var beforeBlock = getUpdatedAt(userId);

        // When
        blockUserRepository.blockUser(userId);

        // Then
        var afterBlock = getUpdatedAt(userId);
        assertThat(afterBlock).isAfterOrEqualTo(beforeBlock);
    }

    private BigInteger insertTestUser() {
        String sql = """
            INSERT INTO users (username, password_hash, role, balance, status, created_at, updated_at)
            VALUES (:username, 'hash', 'CUSTOMER', 0, :status, NOW(), NOW())
            RETURNING id
            """;

        var params = new MapSqlParameterSource()
            .addValue("username", "testuser")
            .addValue("status", UserStatusEnum.ACTIVE.name());

        var keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(sql, params, keyHolder);

        return BigInteger.valueOf(keyHolder.getKey().longValue());
    }

    private UserStatusEnum getUserStatus(BigInteger userId) {
        String sql = "SELECT status FROM users WHERE id = :userId";
        var params = new MapSqlParameterSource()
            .addValue("userId", userId);

        String status = jdbcTemplate.queryForObject(sql, params, String.class);
        return UserStatusEnum.fromValue(status);
    }

    private LocalDateTime getUpdatedAt(BigInteger userId) {
        String sql = "SELECT updated_at FROM users WHERE id = :userId";
        var params = new MapSqlParameterSource()
            .addValue("userId", userId);

        return jdbcTemplate.queryForObject(sql, params, Timestamp.class).toLocalDateTime();
    }
}
