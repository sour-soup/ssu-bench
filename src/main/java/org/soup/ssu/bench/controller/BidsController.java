package org.soup.ssu.bench.controller;

import lombok.RequiredArgsConstructor;
import org.soup.ssu.bench.config.security.AuthenticatedUser;
import org.soup.ssu.bench.config.security.AuthenticatedUserContext;
import org.soup.ssu.bench.service.BidService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import ssu.bench.endpoint.BidsApi;
import ssu.bench.model.BidResponse;
import ssu.bench.model.PageBidResponse;

import java.math.BigInteger;

@RestController
@RequiredArgsConstructor
public class BidsController implements BidsApi {

    private final AuthenticatedUserContext userContext;
    private final BidService bidService;

    @Override
    public ResponseEntity<BidResponse> getBidById(BigInteger bidId) {
        BidResponse bidResponse = bidService.getBidById(bidId);
        return ResponseEntity.ok(bidResponse);
    }

    @Override
    public ResponseEntity<PageBidResponse> getTaskBids(BigInteger taskId, Integer page, Integer size) {
        PageBidResponse pageBidResponse = bidService.getTaskBids(taskId, page, size);
        return ResponseEntity.ok(pageBidResponse);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') || hasRole('CUSTOMER')")
    public ResponseEntity<BidResponse> postAcceptBid(BigInteger bidId) {
        AuthenticatedUser user = userContext.getAuthenticatedUser();
        BidResponse bidResponse = bidService.acceptBid(bidId, user.id());
        return ResponseEntity.ok(bidResponse);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') || hasRole('EXECUTOR')")
    public ResponseEntity<BidResponse> postCreateBid(BigInteger taskId) {
        AuthenticatedUser user = userContext.getAuthenticatedUser();
        BidResponse bidResponse = bidService.createBid(taskId, user.id());
        return ResponseEntity.ok(bidResponse);
    }
}
