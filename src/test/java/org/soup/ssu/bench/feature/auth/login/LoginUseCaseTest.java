package org.soup.ssu.bench.feature.auth.login;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.soup.ssu.bench.exception.BadRequestException;
import org.soup.ssu.bench.exception.EntityNotFoundException;
import org.soup.ssu.bench.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import ssu.bench.model.AuthResponse;
import ssu.bench.model.LoginRequest;
import ssu.bench.model.RoleEnum;

import java.math.BigInteger;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseTest {

    @Mock
    private LoginRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private LoginUseCase loginUseCase;

    private LoginRequest loginRequest;
    private LoginCredentials credentials;
    private final String TOKEN = "jwt-token-123";

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest()
            .username("testuser")
            .password("password123");

        credentials = new LoginCredentials(
            BigInteger.valueOf(1L),
            "$2a$10$hashedPassword",
            RoleEnum.CUSTOMER
        );
    }

    @Test
    @DisplayName("Успешный логин возвращает токен")
    void shouldReturnTokenWhenCredentialsAreValid() {
        // Given
        given(repository.findCredentialsByUsername(loginRequest.getUsername()))
            .willReturn(Optional.of(credentials));
        given(passwordEncoder.matches(loginRequest.getPassword(), credentials.passwordHash()))
            .willReturn(true);
        given(jwtService.createToken(loginRequest.getUsername(), credentials.id(), credentials.role()))
            .willReturn(TOKEN);

        // When
        AuthResponse response = loginUseCase.execute(loginRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo(TOKEN);
        verify(repository).findCredentialsByUsername(loginRequest.getUsername());
        verify(passwordEncoder).matches(loginRequest.getPassword(), credentials.passwordHash());
        verify(jwtService).createToken(loginRequest.getUsername(), credentials.id(), credentials.role());
    }

    @Test
    @DisplayName("Бросает EntityNotFoundException когда пользователь не найден")
    void shouldThrowEntityNotFoundExceptionWhenUserNotFound() {
        // Given
        given(repository.findCredentialsByUsername(loginRequest.getUsername()))
            .willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> loginUseCase.execute(loginRequest))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessage("User not found");

        verify(repository).findCredentialsByUsername(loginRequest.getUsername());
        verify(passwordEncoder, org.mockito.Mockito.never())
            .matches(any(), any());
        verify(jwtService, org.mockito.Mockito.never())
            .createToken(any(), any(), any());
    }

    @Test
    @DisplayName("Бросает BadRequestException когда пароль неверный")
    void shouldThrowBadRequestExceptionWhenPasswordIsInvalid() {
        // Given
        given(repository.findCredentialsByUsername(loginRequest.getUsername()))
            .willReturn(Optional.of(credentials));
        given(passwordEncoder.matches(loginRequest.getPassword(), credentials.passwordHash()))
            .willReturn(false);

        // When & Then
        assertThatThrownBy(() -> loginUseCase.execute(loginRequest))
            .isInstanceOf(BadRequestException.class)
            .hasMessage("Invalid password");

        verify(repository).findCredentialsByUsername(loginRequest.getUsername());
        verify(passwordEncoder).matches(loginRequest.getPassword(), credentials.passwordHash());
        verify(jwtService, org.mockito.Mockito.never())
            .createToken(any(), any(), any());
    }
}
