package com.sh.Ram.enums;

public enum TradeStatus {
    WAITING_DELIVERY,   // 판매자 배송 등록 대기
    SHIPPING,           // 배송중
    DELIVERED,          // 배송 완료
    CONFIRMED,          // 구매자 수령 확인
    CANCELLED
}