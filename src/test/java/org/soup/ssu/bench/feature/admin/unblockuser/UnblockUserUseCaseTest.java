package org.soup.ssu.bench.feature.admin.unblockuser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.soup.ssu.bench.exception.EntityNotFoundException;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UnblockUserUseCaseTest {

    @Mock
    private UnblockUserRepository repository;

    @InjectMocks
    private UnblockUserUseCase unblockUserUseCase;

    private BigInteger userId;

    @BeforeEach
    void setUp() {
        userId = BigInteger.valueOf(1L);
    }

    @Test
    @DisplayName("Успешная разблокировка существующего пользователя")
    void shouldUnblockExistingUser() {
        // Given
        given(repository.userExists(userId)).willReturn(true);

        // When
        unblockUserUseCase.execute(userId);

        // Then
        verify(repository).userExists(userId);
        verify(repository).unblockUser(userId);
    }

    @Test
    @DisplayName("Бросает EntityNotFoundException когда пользователь не найден")
    void shouldThrowEntityNotFoundExceptionWhenUserNotFound() {
        // Given
        given(repository.userExists(userId)).willReturn(false);

        // When & Then
        assertThatThrownBy(() -> unblockUserUseCase.execute(userId))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("User");

        verify(repository).userExists(userId);
        verify(repository, never()).unblockUser(userId);
    }
}
