package org.soup.ssu.bench.controller;

import lombok.RequiredArgsConstructor;
import org.soup.ssu.bench.security.AuthenticatedUser;
import org.soup.ssu.bench.security.AuthenticatedUserContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ssu.bench.endpoint.BidsApi;
import ssu.bench.model.BidResponse;
import ssu.bench.model.PageBidResponse;

import java.math.BigInteger;

@RestController
@RequiredArgsConstructor
public class BidsController implements BidsApi {

    private final AuthenticatedUserContext userContext;

    @Override
    public ResponseEntity<BidResponse> getBidById(BigInteger bidId) {
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<PageBidResponse> getMyBids(Integer page, Integer size) {
        AuthenticatedUser user = userContext.getAuthenticatedUser();
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<PageBidResponse> getTaskBids(BigInteger taskId, Integer page, Integer size) {
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<BidResponse> postAcceptBid(BigInteger bidId) {
        AuthenticatedUser user = userContext.getAuthenticatedUser();
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<BidResponse> postCreateBid(BigInteger taskId) {
        return ResponseEntity.ok().build();
    }
}
