package com.sh.Ram.common.exception.bid;

import lombok.Getter;

@Getter
public class BidException extends RuntimeException {

    private final Integer currentPrice;
    private final Integer nextBidPrice;

    public BidException(String msg) {
        super(msg);
        this.currentPrice = null;
        this.nextBidPrice = null;
    }

    public BidException(String msg, Integer currentPrice, Integer nextBidPrice) {
        super(msg);
        this.currentPrice = currentPrice;
        this.nextBidPrice = nextBidPrice;
    }
}
