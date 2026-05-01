package org.soup.ssu.bench.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ssu.bench.endpoint.AdminApi;
import ssu.bench.model.PageUserResponse;

import java.math.BigInteger;

@RestController
public class AdminController implements AdminApi {

    @Override
    public ResponseEntity<PageUserResponse> getListUsers(Integer page, Integer size) {
        return null;
    }

    @Override
    public ResponseEntity<Void> postBlockUser(BigInteger userId) {
        return null;
    }

    @Override
    public ResponseEntity<Void> postUnblockUser(BigInteger userId) {
        return null;
    }
}
