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
    public ResponseEntity<BidResponse> bidsBidIdAcceptPost(BigInteger bidId) {
        return null;
    }

    @Override
    public ResponseEntity<PageBidResponse> tasksTaskIdBidsGet(BigInteger taskId, Integer page, Integer size) {
        return null;
    }

    @Override
    public ResponseEntity<BidResponse> tasksTaskIdBidsPost(BigInteger taskId) {
        return null;
    }
}
