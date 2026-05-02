package org.soup.ssu.bench.feature.auth.login;

import lombok.RequiredArgsConstructor;
import org.soup.ssu.bench.exception.BadRequestException;
import org.soup.ssu.bench.exception.EntityNotFoundException;
import org.soup.ssu.bench.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ssu.bench.model.AuthResponse;
import ssu.bench.model.LoginRequest;

@Component
@RequiredArgsConstructor
public class LoginUseCase {

    private final LoginRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse execute(LoginRequest request) {
        LoginCredentials credentials = repository.findCredentialsByUsername(request.getUsername())
            .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), credentials.passwordHash())) {
            throw new BadRequestException("Invalid password");
        }

        String token = jwtService.createToken(request.getUsername(), credentials.id(), credentials.role());

        return new AuthResponse().accessToken(token);
    }
}
