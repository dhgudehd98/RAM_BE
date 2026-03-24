package com.sh.Ram.common.exception.auctionResult;

public class AuctionResultException extends RuntimeException {

    public AuctionResultException(String msg) {
        super(msg);
    }

    public AuctionResultException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
