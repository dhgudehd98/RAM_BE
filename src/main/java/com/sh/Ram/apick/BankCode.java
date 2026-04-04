package com.sh.Ram.apick;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum BankCode {

    SHINHAN("088", "신한"),
    JEJU("035", "제주"),
    KOOKMIN("004", "국민"),
    IBK("003", "기업"),
    NH("011", "농협"),
    KDB("002", "산업"),
    SUHYEOP("007", "수협"),
    SHINHYUP("048", "신협"),
    WOORI("020", "우리"),
    HANA("081", "하나"),
    CITI("027", "한국씨티"),
    KAKAO("090", "카카오뱅크"),
    K("089", "케이뱅크"),
    TOSS("092", "토스뱅크"),
    GYEONGNAM("039", "경남"),
    GWANGJU("034", "광주"),
    DAEGU("031", "대구"),
    BUSAN("032", "부산"),
    JEONBUK("037", "전북"),
    SAEMAUL("045", "새마을"),
    POST_OFFICE("071", "우체국"),
    SAVINGS_BANK("050", "저축은행"),
    LOCAL_NONGHYUP("012", "지역농.축협"),
    DEUTSCHE_BANK("055", "도이치"),
    BANK_OF_CHINA("063", "중국"),
    CHINA_CONSTRUCTION_BANK("067", "중국건설"),
    INDUSTRIAL_AND_COMMERCIAL_BANK_OF_CHINA("062", "중국공상"),
    BNP_PARIBAS("061", "BNP파리바"),
    BANK_OF_AMERICA("060", "BOA"),
    HSBC("054", "HSBC"),
    JPMORGAN_CHASE("057", "JP모간"),
    STANDARD_CHARTERED("023", "SC"),
    NFCF("064", "산림조합");

    private final String code;
    private final String name;

    BankCode(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static BankCode fromCode(String code) {
        return Arrays.stream(values())
                .filter(b -> b.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(code + "는 지원하지 않는 코드입니다."));
    }

    public static BankCode fromName(String name) {
        return Arrays.stream(values())
                .filter(b -> b.name.equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(name + "는 지원하지 않는 은행명입니다."));
    }
}
