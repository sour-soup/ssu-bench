package org.soup.ssu.bench.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.soup.ssu.bench.config.security.JwtService;
import org.soup.ssu.bench.exception.BadRequestException;
import org.soup.ssu.bench.repository.UserRepository;
import org.soup.ssu.bench.repository.entity.UserEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ssu.bench.model.AuthResponse;
import ssu.bench.model.LoginRequest;
import ssu.bench.model.RegisterRequest;
import ssu.bench.model.RoleEnum;
import ssu.bench.model.UserStatusEnum;

import java.math.BigInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final BigInteger USER_START_BALANCE = BigInteger.ZERO;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse login(LoginRequest request) {
        UserEntity userEntity = userRepository.getUserByUsername(request.getUsername())
            .orElseThrow(() -> new BadRequestException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), userEntity.passwordHash())) {
            throw new BadRequestException("Invalid username or password");
        }

        String token = jwtService.createToken(request.getUsername(), userEntity.id(), userEntity.role());

        return new AuthResponse().accessToken(token);
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.getUserByUsername(request.getUsername()).isPresent()) {
            throw new BadRequestException("User with username '%s' already exists".formatted(request.getUsername()));
        }

        if (request.getRole() == RoleEnum.ADMIN) {
            throw new BadRequestException("Cannot register as ADMIN directly");
        }

        UserEntity userEntity = userRepository.createUser(buildUserEntity(request));
        String token = jwtService.createToken(request.getUsername(), userEntity.id(), request.getRole().getValue());

        return new AuthResponse().accessToken(token);
    }

    private UserEntity buildUserEntity(RegisterRequest request) {
        String passwordHash = passwordEncoder.encode(request.getPassword());

        return UserEntity.builder()
            .username(request.getUsername())
            .passwordHash(passwordHash)
            .role(request.getRole().getValue())
            .status(UserStatusEnum.ACTIVE.getValue())
            .balance(USER_START_BALANCE)
            .build();
    }
}
