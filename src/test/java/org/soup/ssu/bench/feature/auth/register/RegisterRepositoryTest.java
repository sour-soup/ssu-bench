package org.soup.ssu.bench.feature.auth.register;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soup.ssu.bench.RepositoryTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import ssu.bench.model.RoleEnum;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;

@Import(RegisterRepository.class)
class RegisterRepositoryTest extends RepositoryTest {

    @Autowired
    private RegisterRepository registerRepository;

    @Test
    @DisplayName("createUser успешно создает пользователя и возвращает id")
    void shouldCreateUserAndReturnId() {
        // Given
        String username = "testuser";
        String passwordHash = "$2a$10$hashedPassword";
        RoleEnum role = RoleEnum.CUSTOMER;

        // When
        BigInteger userId = registerRepository.createUser(username, passwordHash, role);

        // Then
        assertThat(userId).isNotNull();
        assertThat(userId.longValue()).isPositive();
    }

    @Test
    @DisplayName("existsByUsername возвращает true когда пользователь существует")
    void shouldReturnTrueWhenUserExists() {
        // Given
        String username = "existinguser";
        registerRepository.createUser(username, "passwordHash", RoleEnum.CUSTOMER);

        // When
        boolean exists = registerRepository.existsByUsername(username);

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByUsername возвращает false когда пользователь не существует")
    void shouldReturnFalseWhenUserDoesNotExist() {
        // Given
        String username = "nonexistentuser";

        // When
        boolean exists = registerRepository.existsByUsername(username);

        // Then
        assertThat(exists).isFalse();
    }
}
