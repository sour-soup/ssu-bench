package org.soup.ssu.bench.feature.admin.blockuser;

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
class BlockUserUseCaseTest {

    @Mock
    private BlockUserRepository repository;

    @InjectMocks
    private BlockUserUseCase blockUserUseCase;

    private static final BigInteger USER_ID = BigInteger.valueOf(1L);

    @Test
    @DisplayName("Успешная блокировка существующего пользователя")
    void shouldBlockExistingUser() {
        // Given
        given(repository.userExists(USER_ID)).willReturn(true);

        // When
        blockUserUseCase.execute(USER_ID);

        // Then
        verify(repository).userExists(USER_ID);
        verify(repository).blockUser(USER_ID);
    }

    @Test
    @DisplayName("Бросает EntityNotFoundException когда пользователь не найден")
    void shouldThrowEntityNotFoundExceptionWhenUserNotFound() {
        // Given
        given(repository.userExists(USER_ID)).willReturn(false);

        // When & Then
        assertThatThrownBy(() -> blockUserUseCase.execute(USER_ID))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("User");

        verify(repository).userExists(USER_ID);
        verify(repository, never()).blockUser(USER_ID);
    }
}
