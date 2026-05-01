package org.soup.ssu.bench.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ssu.bench.endpoint.BidsApi;
import ssu.bench.model.BidResponse;
import ssu.bench.model.PageBidResponse;

import java.math.BigInteger;

@RestController
public class BidsController implements BidsApi {
    @Override
    public ResponseEntity<PageBidResponse> getTaskBids(BigInteger taskId, Integer page, Integer size) {
        return null;
    }

    @Override
    public ResponseEntity<BidResponse> postAcceptBid(BigInteger bidId) {
        return null;
    }

    @Override
    public ResponseEntity<BidResponse> postCreateBid(BigInteger taskId) {
        return null;
    }
}
