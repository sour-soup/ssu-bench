package org.soup.ssu.bench.controller;

import lombok.RequiredArgsConstructor;
import org.soup.ssu.bench.feature.admin.blockuser.BlockUserUseCase;
import org.soup.ssu.bench.feature.admin.listusers.ListUsersUseCase;
import org.soup.ssu.bench.feature.admin.unblockuser.UnblockUserUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ssu.bench.endpoint.AdminApi;
import ssu.bench.model.PageUserResponse;

import java.math.BigInteger;

@RestController
@RequiredArgsConstructor
public class AdminController implements AdminApi {

    private final ListUsersUseCase listUsersUseCase;
    private final BlockUserUseCase blockUserUseCase;
    private final UnblockUserUseCase unblockUserUseCase;

    @Override
    public ResponseEntity<PageUserResponse> getListUsers(Integer page, Integer size) {
        PageUserResponse response = listUsersUseCase.execute(page, size);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> postBlockUser(BigInteger userId) {
        blockUserUseCase.execute(userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    public ResponseEntity<Void> postUnblockUser(BigInteger userId) {
        unblockUserUseCase.execute(userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
