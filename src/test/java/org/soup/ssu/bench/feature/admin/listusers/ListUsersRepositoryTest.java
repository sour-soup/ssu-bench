package org.soup.ssu.bench.feature.admin.listusers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soup.ssu.bench.RepositoryTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import ssu.bench.model.RoleEnum;
import ssu.bench.model.UserResponse;
import ssu.bench.model.UserStatusEnum;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Import(ListUsersRepository.class)
class ListUsersRepositoryTest extends RepositoryTest {

    @Autowired
    private ListUsersRepository listUsersRepository;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        cleanupUsers();
        insertTestUsers();
    }

    @Test
    @DisplayName("getUsers возвращает список пользователей")
    void shouldReturnListOfUsers() {
        // When
        List<UserResponse> result = listUsersRepository.getUsers(0, 10);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
    }

    @Test
    @DisplayName("getUsers возвращает правильную пагинацию")
    void shouldReturnCorrectPagination() {
        // When
        List<UserResponse> page0 = listUsersRepository.getUsers(0, 2);
        List<UserResponse> page1 = listUsersRepository.getUsers(1, 2);

        // Then
        assertThat(page0).hasSize(2);
        assertThat(page1).hasSize(1);
        assertThat(page0.get(0).getId())
            .isNotEqualTo(page1.get(0).getId());
    }

    @Test
    @DisplayName("getUsers возвращает пустой список когда пользователей нет")
    void shouldReturnEmptyListWhenNoUsers() {
        // Given
        cleanupUsers();

        // When
        List<UserResponse> result = listUsersRepository.getUsers(0, 10);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getUsers сортирует пользователей по created_at DESC")
    void shouldSortUsersByCreatedAtDesc() {
        // When
        List<UserResponse> result = listUsersRepository.getUsers(0, 10);

        // Then
        assertThat(result).hasSize(3);
        LocalDateTime firstCreatedAt = result.get(0).getCreatedAt();
        LocalDateTime secondCreatedAt = result.get(1).getCreatedAt();
        assertThat(firstCreatedAt).isAfterOrEqualTo(secondCreatedAt);
    }

    @Test
    @DisplayName("getUsers корректно маппит данные пользователя")
    void shouldMapUserDataCorrectly() {
        // When
        List<UserResponse> result = listUsersRepository.getUsers(0, 10);

        // Then
        UserResponse firstUser = result.getFirst();
        assertThat(firstUser.getId()).isNotNull();
        assertThat(firstUser.getUsername()).isNotNull();
        assertThat(firstUser.getRole()).isIn(RoleEnum.CUSTOMER, RoleEnum.EXECUTOR, RoleEnum.ADMIN);
        assertThat(firstUser.getStatus()).isIn(UserStatusEnum.ACTIVE, UserStatusEnum.BLOCKED);
        assertThat(firstUser.getBalance()).isNotNull();
    }

    @Test
    @DisplayName("getUsers с большим размером страницы возвращает всех пользователей")
    void shouldReturnAllUsersWithLargePageSize() {
        // When
        List<UserResponse> result = listUsersRepository.getUsers(0, 100);

        // Then
        assertThat(result).hasSize(3);
    }

    @Test
    @DisplayName("getUsers со страницей за пределами данных возвращает пустой список")
    void shouldReturnEmptyListForPageOutOfBounds() {
        // When
        List<UserResponse> result = listUsersRepository.getUsers(100, 10);

        // Then
        assertThat(result).isEmpty();
    }

    private void cleanupUsers() {
        jdbcTemplate.update("DELETE FROM users", new HashMap<>());
    }

    private void insertTestUsers() {
        String sql = """
            INSERT INTO users (username, password_hash, role, balance, status, created_at, updated_at)
            VALUES (:username, 'hash', :role, :balance, :status, :createdAt, :updatedAt)
            """;

        LocalDateTime now = LocalDateTime.now();

        for (int i = 0; i < 3; i++) {
            var params = new org.springframework.jdbc.core.namedparam.MapSqlParameterSource()
                .addValue("username", "user" + i)
                .addValue("role", i == 0 ? "CUSTOMER" : i == 1 ? "EXECUTOR" : "ADMIN")
                .addValue("balance", i * 100L)
                .addValue("status", "ACTIVE")
                .addValue("createdAt", now.minusDays(3 - i))
                .addValue("updatedAt", now);

            jdbcTemplate.update(sql, params);
        }
    }
}
