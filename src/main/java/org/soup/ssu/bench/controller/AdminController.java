package org.soup.ssu.bench.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ssu.bench.endpoint.AdminApi;
import ssu.bench.model.PageUserResponse;

import java.math.BigInteger;

@RestController
public class AdminController implements AdminApi {
    @Override
    public ResponseEntity<PageUserResponse> adminUsersGet(Integer page, Integer size) {
        return null;
    }

    @Override
    public ResponseEntity<Void> adminUsersUserIdBlockPost(BigInteger userId) {
        return null;
    }

    @Override
    public ResponseEntity<Void> adminUsersUserIdUnblockPost(BigInteger userId) {
        return null;
    }
}
