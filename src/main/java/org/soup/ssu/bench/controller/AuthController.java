package org.soup.ssu.bench.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.soup.ssu.bench.feature.auth.register.RegisterUseCase;
import ssu.bench.endpoint.AuthApi;
import ssu.bench.model.AuthResponse;
import ssu.bench.model.LoginRequest;
import ssu.bench.model.RegisterRequest;

@RestController
@RequiredArgsConstructor
public class AuthController implements AuthApi {

    private final RegisterUseCase registerUseCase;

    @Override
    public ResponseEntity<AuthResponse> postAuthLogin(LoginRequest loginRequest) {
        return null;
    }

    @Override
    public ResponseEntity<AuthResponse> postAuthRegister(RegisterRequest registerRequest) {
        AuthResponse response = registerUseCase.execute(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
