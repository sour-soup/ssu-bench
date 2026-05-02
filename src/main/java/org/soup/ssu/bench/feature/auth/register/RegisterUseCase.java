package org.soup.ssu.bench.feature.auth.register;

import lombok.RequiredArgsConstructor;
import org.soup.ssu.bench.exception.BadRequestException;
import org.soup.ssu.bench.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ssu.bench.model.AuthResponse;
import ssu.bench.model.RegisterRequest;
import ssu.bench.model.RoleEnum;

import java.math.BigInteger;

@Component
@RequiredArgsConstructor
public class RegisterUseCase {

    private final RegisterRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse execute(RegisterRequest request) {
        if (repository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("User with username '%s' already exists".formatted(request.getUsername()));
        }

        if (request.getRole() == RoleEnum.ADMIN) {
            throw new BadRequestException("Cannot register as ADMIN directly");
        }

        String passwordHash = passwordEncoder.encode(request.getPassword());
        BigInteger userId = repository.createUser(request.getUsername(), passwordHash, request.getRole());
        String token = jwtService.createToken(request.getUsername(), userId, request.getRole());

        return new AuthResponse().accessToken(token);
    }
}
