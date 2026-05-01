package org.soup.ssu.bench.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ssu.bench.endpoint.AuthApi;
import ssu.bench.model.AuthResponse;
import ssu.bench.model.LoginRequest;
import ssu.bench.model.RegisterRequest;

@RestController
public class AuthController implements AuthApi {
    @Override
    public ResponseEntity<AuthResponse> authLoginPost(LoginRequest loginRequest) {
        return null;
    }

    @Override
    public ResponseEntity<AuthResponse> authRegisterPost(RegisterRequest registerRequest) {
        return null;
    }
}
