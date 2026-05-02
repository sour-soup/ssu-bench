package org.soup.ssu.bench.feature.auth.login;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soup.ssu.bench.RepositoryTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import ssu.bench.model.RoleEnum;

import java.math.BigInteger;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Import(LoginRepository.class)
class LoginRepositoryTest extends RepositoryTest {

    @Autowired
    private LoginRepository loginRepository;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("findCredentialsByUsername возвращает данные когда пользователь существует")
    void shouldFindCredentialsWhenUserExists() {
        // Given
        String username = "testuser";
        String passwordHash = "$2a$10$hashedPassword";
        RoleEnum role = RoleEnum.CUSTOMER;

        insertTestUser(username, passwordHash, role);

        // When
        Optional<LoginCredentials> credentials = loginRepository.findCredentialsByUsername(username);

        // Then
        assertThat(credentials).isPresent();
        assertThat(credentials.get().id()).isNotNull();
        assertThat(credentials.get().passwordHash()).isEqualTo(passwordHash);
        assertThat(credentials.get().role()).isEqualTo(role);
    }

    @Test
    @DisplayName("findCredentialsByUsername возвращает пустой Optional когда пользователь не существует")
    void shouldReturnEmptyWhenUserDoesNotExist() {
        // Given
        String username = "nonexistentuser";

        // When
        Optional<LoginCredentials> credentials = loginRepository.findCredentialsByUsername(username);

        // Then
        assertThat(credentials).isEmpty();
    }

    @Test
    @DisplayName("findCredentialsByUsername корректно маппит разные роли")
    void shouldMapDifferentRolesCorrectly() {
        // Given
        insertTestUser("customer_user", "hash1", RoleEnum.CUSTOMER);
        insertTestUser("executor_user", "hash2", RoleEnum.EXECUTOR);
        insertTestUser("admin_user", "hash3", RoleEnum.ADMIN);

        // When & Then
        Optional<LoginCredentials> customer = loginRepository.findCredentialsByUsername("customer_user");
        assertThat(customer).isPresent();
        assertThat(customer.get().role()).isEqualTo(RoleEnum.CUSTOMER);

        Optional<LoginCredentials> executor = loginRepository.findCredentialsByUsername("executor_user");
        assertThat(executor).isPresent();
        assertThat(executor.get().role()).isEqualTo(RoleEnum.EXECUTOR);

        Optional<LoginCredentials> admin = loginRepository.findCredentialsByUsername("admin_user");
        assertThat(admin).isPresent();
        assertThat(admin.get().role()).isEqualTo(RoleEnum.ADMIN);
    }

    @Test
    @DisplayName("findCredentialsByUsername чувствителен к регистру имени пользователя")
    void shouldBeCaseSensitive() {
        // Given
        insertTestUser("TestUser", "hash1", RoleEnum.CUSTOMER);

        // When
        Optional<LoginCredentials> exactCase = loginRepository.findCredentialsByUsername("TestUser");
        Optional<LoginCredentials> lowerCase = loginRepository.findCredentialsByUsername("testuser");
        Optional<LoginCredentials> upperCase = loginRepository.findCredentialsByUsername("TESTUSER");

        // Then
        assertThat(exactCase).isPresent();
        assertThat(lowerCase).isEmpty();
        assertThat(upperCase).isEmpty();
    }

    private void insertTestUser(String username, String passwordHash, RoleEnum role) {
        String sql = """
            INSERT INTO users (username, password_hash, role, balance, status)
            VALUES (:username, :passwordHash, :role, 0, 'ACTIVE')
            """;
        
        var params = new org.springframework.jdbc.core.namedparam.MapSqlParameterSource()
            .addValue("username", username)
            .addValue("passwordHash", passwordHash)
            .addValue("role", role.name());
        
        jdbcTemplate.update(sql, params);
    }
}
