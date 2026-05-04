package org.soup.ssu.bench.controller;

import lombok.RequiredArgsConstructor;
import org.soup.ssu.bench.service.AdminService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import ssu.bench.endpoint.AdminApi;
import ssu.bench.model.PageUserResponse;

import java.math.BigInteger;

@RestController
@RequiredArgsConstructor
public class AdminController implements AdminApi {

    private final AdminService adminService;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageUserResponse> getListUsers(Integer page, Integer size) {
        PageUserResponse response = adminService.getUsers(page, size);
        return ResponseEntity.ok(response);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> postBlockUser(BigInteger userId) {
        adminService.blockUser(userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> postUnblockUser(BigInteger userId) {
        adminService.unblockUser(userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
