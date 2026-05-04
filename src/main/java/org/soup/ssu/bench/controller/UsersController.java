package org.soup.ssu.bench.controller;

import lombok.RequiredArgsConstructor;
import org.soup.ssu.bench.security.AuthenticatedUser;
import org.soup.ssu.bench.security.AuthenticatedUserContext;
import org.soup.ssu.bench.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ssu.bench.endpoint.UsersApi;
import ssu.bench.model.DepositRequest;
import ssu.bench.model.UserResponse;

@RestController
@RequiredArgsConstructor
public class UsersController implements UsersApi {

    private final AuthenticatedUserContext userContext;
    private final UserService userService;

    @Override
    public ResponseEntity<UserResponse> getMyProfile() {
        AuthenticatedUser user = userContext.getAuthenticatedUser();
        UserResponse response = userService.getUser(user.id());

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<UserResponse> postDeposit(DepositRequest depositRequest) {
        AuthenticatedUser user = userContext.getAuthenticatedUser();
        UserResponse response = userService.deposit(user.id(), depositRequest.getAmount());

        return ResponseEntity.ok(response);
    }
}
