package com.sh.Ram.common.exception;

import com.sh.Ram.common.exception.account.AccountException;
import com.sh.Ram.common.exception.accountHistory.AccountHistoryException;
import com.sh.Ram.common.exception.auction.AuctionException;
import com.sh.Ram.common.exception.auctionResult.AuctionResultException;
import com.sh.Ram.common.exception.bid.BidException;
import com.sh.Ram.common.exception.member.MemberException;
import com.sh.Ram.common.exception.product.ProductException;
import jakarta.security.auth.message.AuthException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<Map<String, String>> handleAuth(AuthException e) {
        Map<String, String> res = new HashMap<>();
        res.put("result", "N");
        res.put("message", e.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
    }

    @ExceptionHandler(ProductException.class)
    public ResponseEntity<Map<String, String>> handleProduct(ProductException e) {
        Map<String, String> res = new HashMap<>();
        res.put("result", "N");
        res.put("message", e.getMessage());
        return ResponseEntity.badRequest().body(res);
    }

    @ExceptionHandler(MemberException.class)
    public ResponseEntity<Map<String, String>> handleMember(MemberException e) {
        Map<String, String> res = new HashMap<>();
        res.put("result", "N");
        res.put("message", e.getMessage());
        return ResponseEntity.badRequest().body(res);
    }

    @ExceptionHandler(AuctionException.class)
    public ResponseEntity<Map<String, String>> handleAuction(AuctionException e) {
        Map<String, String> res = new HashMap<>();
        res.put("result", "N");
        res.put("message", e.getMessage());
        return ResponseEntity.badRequest().body(res);
    }

    @ExceptionHandler(AuctionResultException.class)
    public ResponseEntity<Map<String, String>> handleAuctionResult(AuctionResultException e) {
        Map<String, String> res = new HashMap<>();
        res.put("result", "N");
        res.put("message", e.getMessage());
        return ResponseEntity.badRequest().body(res);
    }

    @ExceptionHandler(AccountException.class)
    public ResponseEntity<Map<String, String>> handleAccount(AccountException e) {
        Map<String, String> res = new HashMap<>();
        res.put("result", "N");
        res.put("message", e.getMessage());
        return ResponseEntity.badRequest().body(res);
    }

    @ExceptionHandler(AccountHistoryException.class)
    public ResponseEntity<Map<String, String>> handleAccountHistory(AccountHistoryException e) {
        Map<String, String> res = new HashMap<>();
        res.put("result", "N");
        res.put("message", e.getMessage());
        return ResponseEntity.badRequest().body(res);
    }

    @ExceptionHandler(BidException.class)
    public ResponseEntity<Map<String, Object>> handleBid(BidException e) {

        Map<String, Object> res = new HashMap<>();
        res.put("message", e.getMessage());

        // 가격 mismatch 시
        if (e.getCurrentPrice() != null) {
            res.put("currentPrice", e.getCurrentPrice());
            res.put("nextBidPrice", e.getNextBidPrice());
        }

        return ResponseEntity.status(HttpStatus.CONFLICT).body(res);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception e) {
        e.printStackTrace();
        log.error("Error Message : {}", e.getMessage());
        Map<String, String> response = new HashMap<>();
        response.put("result", "N");
        response.put("message", "시스템 오류가 발생했습니다. 관리자에게 문의해주세요.");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

}