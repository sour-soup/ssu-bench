package org.soup.ssu.bench.feature.auth.register;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.soup.ssu.bench.exception.BadRequestException;
import org.soup.ssu.bench.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import ssu.bench.model.AuthResponse;
import ssu.bench.model.RegisterRequest;
import ssu.bench.model.RoleEnum;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RegisterUseCaseTest {

    @Mock
    private RegisterRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private RegisterUseCase registerUseCase;

    private RegisterRequest registerRequest;
    private final String TOKEN = "jwt-token-123";
    private final String PASSWORD_HASH = "$2a$10$hashedPassword";
    private final BigInteger USER_ID = BigInteger.valueOf(1L);

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest()
            .username("newuser")
            .password("password123")
            .role(RoleEnum.CUSTOMER);
    }

    @Test
    @DisplayName("Успешная регистрация возвращает токен")
    void shouldReturnTokenWhenRegistrationSuccessful() {
        // Given
        given(repository.existsByUsername(registerRequest.getUsername())).willReturn(false);
        given(passwordEncoder.encode(registerRequest.getPassword())).willReturn(PASSWORD_HASH);
        given(repository.createUser(registerRequest.getUsername(), PASSWORD_HASH, registerRequest.getRole()))
            .willReturn(USER_ID);
        given(jwtService.createToken(registerRequest.getUsername(), USER_ID, registerRequest.getRole()))
            .willReturn(TOKEN);

        // When
        AuthResponse response = registerUseCase.execute(registerRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo(TOKEN);
        verify(repository).existsByUsername(registerRequest.getUsername());
        verify(passwordEncoder).encode(registerRequest.getPassword());
        verify(repository).createUser(registerRequest.getUsername(), PASSWORD_HASH, registerRequest.getRole());
        verify(jwtService).createToken(registerRequest.getUsername(), USER_ID, registerRequest.getRole());
    }

    @Test
    @DisplayName("Бросает BadRequestException когда пользователь уже существует")
    void shouldThrowBadRequestExceptionWhenUserAlreadyExists() {
        // Given
        given(repository.existsByUsername(registerRequest.getUsername())).willReturn(true);

        // When & Then
        assertThatThrownBy(() -> registerUseCase.execute(registerRequest))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("already exists");

        verify(repository).existsByUsername(registerRequest.getUsername());
        verify(passwordEncoder, never()).encode(any());
        verify(repository, never()).createUser(any(), any(), any());
        verify(jwtService, never()).createToken(any(), any(), any());
    }

    @Test
    @DisplayName("Бросает BadRequestException при попытке зарегистрироваться как ADMIN")
    void shouldThrowBadRequestExceptionWhenRegisteringAsAdmin() {
        // Given
        registerRequest.setRole(RoleEnum.ADMIN);
        given(repository.existsByUsername(registerRequest.getUsername())).willReturn(false);

        // When & Then
        assertThatThrownBy(() -> registerUseCase.execute(registerRequest))
            .isInstanceOf(BadRequestException.class)
            .hasMessage("Cannot register as ADMIN directly");

        verify(repository).existsByUsername(registerRequest.getUsername());
        verify(passwordEncoder, never()).encode(any());
        verify(repository, never()).createUser(any(), any(), any());
        verify(jwtService, never()).createToken(any(), any(), any());
    }

    @Test
    @DisplayName("Успешная регистрация с ролью EXECUTOR")
    void shouldReturnTokenWhenRegisteringAsExecutor() {
        // Given
        registerRequest.setRole(RoleEnum.EXECUTOR);
        given(repository.existsByUsername(registerRequest.getUsername())).willReturn(false);
        given(passwordEncoder.encode(registerRequest.getPassword())).willReturn(PASSWORD_HASH);
        given(repository.createUser(registerRequest.getUsername(), PASSWORD_HASH, registerRequest.getRole()))
            .willReturn(USER_ID);
        given(jwtService.createToken(registerRequest.getUsername(), USER_ID, registerRequest.getRole()))
            .willReturn(TOKEN);

        // When
        AuthResponse response = registerUseCase.execute(registerRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo(TOKEN);
    }
}
