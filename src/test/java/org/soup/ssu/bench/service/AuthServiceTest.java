package org.soup.ssu.bench.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.soup.ssu.bench.exception.BadRequestException;
import org.soup.ssu.bench.repository.UserRepository;
import org.soup.ssu.bench.repository.entity.UserEntity;
import org.soup.ssu.bench.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import ssu.bench.model.AuthResponse;
import ssu.bench.model.LoginRequest;
import ssu.bench.model.RegisterRequest;
import ssu.bench.model.RoleEnum;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.soup.ssu.bench.generator.EntityGenerator.PASSWORD;
import static org.soup.ssu.bench.generator.EntityGenerator.PASSWORD_HASH;
import static org.soup.ssu.bench.generator.EntityGenerator.TOKEN;
import static org.soup.ssu.bench.generator.EntityGenerator.USERNAME;
import static org.soup.ssu.bench.generator.EntityGenerator.CUSTOMER_ID;
import static org.soup.ssu.bench.generator.EntityGenerator.buildUserEntity;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void givenNewUser_whenRegister_thenReturnToken() {
        // given
        RegisterRequest registerRequest = buildRegisterRequest();
        when(userRepository.getUserByUsername(USERNAME)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn(PASSWORD_HASH);

        UserEntity userEntity = buildUserEntity();
        when(userRepository.createUser(userEntity)).thenReturn(userEntity.withId(CUSTOMER_ID));
        when(jwtService.createToken(registerRequest.getUsername(), CUSTOMER_ID, registerRequest.getRole().getValue()))
            .thenReturn(TOKEN);

        // when
        AuthResponse response = authService.register(registerRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo(TOKEN);
    }

    @Test
    void givenExistUser_whenRegister_thenThrowBadRequest() {
        // given
        RegisterRequest registerRequest = buildRegisterRequest();
        when(userRepository.getUserByUsername(USERNAME)).thenReturn(Optional.of(buildUserEntity()));

        // when & then
        assertThatThrownBy(() -> authService.register(registerRequest))
            .isInstanceOf(BadRequestException.class);

        verifyNoMoreInteractions(userRepository, jwtService, passwordEncoder);
    }

    @Test
    void givenAdminUser_whenRegister_thenThrowBadRequest() {
        // given
        RegisterRequest registerRequest = buildRegisterRequest().role(RoleEnum.ADMIN);
        when(userRepository.getUserByUsername(USERNAME)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.register(registerRequest))
            .isInstanceOf(BadRequestException.class);

        verifyNoMoreInteractions(userRepository, jwtService, passwordEncoder);
    }

    @Test
    void givenExistUser_whenLogin_thenReturnToken() {
        // given
        LoginRequest loginRequest = buildLoginRequest();
        UserEntity userEntity = buildUserEntity().withId(CUSTOMER_ID);
        when(userRepository.getUserByUsername(USERNAME)).thenReturn(Optional.of(userEntity));
        when(passwordEncoder.matches(loginRequest.getPassword(), PASSWORD_HASH)).thenReturn(true);

        when(jwtService.createToken(loginRequest.getUsername(), CUSTOMER_ID, userEntity.role()))
            .thenReturn(TOKEN);

        // when
        AuthResponse response = authService.login(loginRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo(TOKEN);
    }

    @Test
    void givenNewUser_whenLogin_thenThrowBadRequest() {
        // given
        LoginRequest loginRequest = buildLoginRequest();
        when(userRepository.getUserByUsername(USERNAME)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.login(loginRequest))
            .isInstanceOf(BadRequestException.class);

        verifyNoMoreInteractions(userRepository, jwtService, passwordEncoder);
    }

    @Test
    void givenInvalidPassword_whenLogin_thenThrowBadRequest() {
        // given
        LoginRequest loginRequest = buildLoginRequest();
        UserEntity userEntity = buildUserEntity().withId(CUSTOMER_ID);
        when(userRepository.getUserByUsername(USERNAME)).thenReturn(Optional.of(userEntity));
        when(passwordEncoder.matches(loginRequest.getPassword(), PASSWORD_HASH)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.login(loginRequest))
            .isInstanceOf(BadRequestException.class);

        verifyNoMoreInteractions(userRepository, jwtService, passwordEncoder);
    }

    private static RegisterRequest buildRegisterRequest() {
        return new RegisterRequest()
            .username(USERNAME)
            .password(PASSWORD)
            .role(RoleEnum.CUSTOMER);
    }

    private static LoginRequest buildLoginRequest() {
        return new LoginRequest()
            .username(USERNAME)
            .password(PASSWORD);
    }
}
