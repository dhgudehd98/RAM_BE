package com.sh.Ram.apick.service;

import com.sh.Ram.apick.BankCode;
import com.sh.Ram.apick.client.ApickClient;
import com.sh.Ram.apick.dto.ApickSendRequest;
import com.sh.Ram.apick.dto.ApickVerifyRequest;
import com.sh.Ram.apick.repository.ApickRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApickService {

    private static final Pattern LAST_4_DIGITS =Pattern.compile("(\\d{4})$");

    private final ApickClient apickClient;
    private final ApickRepository apickRepository;

    /**
     * 1원 인증 요청
     * -Apick 응답의 "입금통장메모"의 뒷 4자리 숫자만 추출하여 Redis 저장
     */
    @Transactional
    public void sendCode(Long memberId, ApickSendRequest req) {

        try {
            // 1) bankCode, bankName 정규화
            BankCode bank = resolveBank(req.bankCode(), req.bankName());

            // 2) Apick api 호출
            Map<String, Object> res = apickClient.transfer1won(
                    req.accountNum(),
                    bank.getCode(),
                    bank.getName(),
                    "Resale-Auction-Market"
            );

            // 3) 응답 검증 및 값 추출
            Map<String, Object> data = getMap(res, "data");
            int success = getInt(data, "success");

            if (success != 1) {
                log.error("[1WON][REQUEST] apick failed response={}", res);
                throw new IllegalStateException("1원 송금 실패");
            }

            String depositMemo = getString(data, "입급통장메모");
            String code = extractLast4Digits(depositMemo);

            // 4) Redis 저장
            apickRepository.saveCode(memberId, code);

            log.info("[1WON][REQUEST] success memberId={}, bankCode={}, depositMemo={}, code4={}",
                    memberId, bank.getCode(), depositMemo, code);
        } catch (Exception e) {
            log.error("[1WON][REQUEST] fail memberId={}", memberId, e);
            throw new IllegalStateException("1원 인증 요청 실패");
        }
    }

    /**
     * 1원 입금 검증
     * memberId + code(4자리)를 받아 Redis와 비교
     * 성공 시 Redis 삭제 + Member.isAccount = true
     */
    @Transactional
    public void verifyCode(Long memberId, ApickVerifyRequest req) {
        try {

            String saved = apickRepository.getCode(memberId)
                    .orElseThrow(() -> new IllegalArgumentException("인증 정보 없음 또는 만료"));

            if (!saved.equals(req.code())) {
                throw new IllegalArgumentException("인증 코드 불일치");
            }

            // 인증 성공 시 Redis 코드 제거
            apickRepository.deleteCode(memberId);
            apickRepository.saveVerified(memberId);

            log.info("[1WON][VERIFY] success memberId={}", memberId);
        } catch (Exception e) {
            log.error("[1WON][VERIFY] fail memberId={}", memberId, e);
            throw e;
        }
    }

    // ===== bankCode/bankName resolve =====
    private BankCode resolveBank(String bankCode, String bankName) {
        if (bankCode != null && !bankCode.isBlank()) {
            return BankCode.fromCode(bankCode);
        }
        if (bankName != null && !bankName.isBlank()) {
            return BankCode.fromName(bankName);
        }
        throw new IllegalArgumentException("bankCode 또는 bankName은 필수입니다.");
    }

    // ===== memo 끝 4자리 추출 =====
    private String extractLast4Digits(String depositMemo) {
        if (depositMemo == null || depositMemo.isBlank()) {
            throw new IllegalArgumentException("입금통장메모가 없습니다.");
        }
        Matcher m = LAST_4_DIGITS.matcher(depositMemo);
        if (!m.find()) {
            throw new IllegalArgumentException("입금통장메모에서 4자리 추출 실패: " + depositMemo);
        }
        return m.group(1);
    }

    // ===== Map parsing helpers (DTO 없이 안정성 확보용) =====
    @SuppressWarnings("unchecked")
    private Map<String, Object> getMap(Map<String, Object> root, String key) {
        Object v = root.get(key);
        if (!(v instanceof Map<?, ?>)) {
            throw new IllegalStateException("응답에 '" + key + "'(Map)가 없습니다.");
        }
        return (Map<String, Object>) v;
    }

    private String getString(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (v == null) {
            throw new IllegalStateException("응답에 '" + key + "'가 없습니다.");
        }
        return String.valueOf(v);
    }

    private int getInt(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (v == null) {
            throw new IllegalStateException("응답에 '" + key + "'가 없습니다.");
        }
        try {
            return Integer.parseInt(String.valueOf(v));
        } catch (NumberFormatException e) {
            throw new IllegalStateException("응답 '" + key + "'가 숫자가 아닙니다: " + v);
        }
    }
}
