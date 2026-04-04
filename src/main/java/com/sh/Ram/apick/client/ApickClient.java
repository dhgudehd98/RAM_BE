package com.sh.Ram.apick.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Slf4j
@Component
public class ApickClient {

    private static final String BASE_URL = "https://apick.app";
    private static final String PATH = "/rest/transfer_1won";

    private final RestClient restClient;

    @Value("${apick.apikey}")
    private String apiKey;

    public ApickClient(RestClient.Builder builder) {
        this.restClient = builder.baseUrl(BASE_URL).build();
    }

    /**
     * Apick 1원 송금 요청
     * 응답은 Map으로 받아서 Service에 필요한 값만 사용
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> transfer1won(
            String accountNum,
            String bankCode,
            String bankName,
            String memo
    ) {
        try {
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("accountNum", accountNum);
            form.add("bankCode", bankCode);
            form.add("bankName", bankName);
            form.add("memo", memo);

            Map<String, Object> res = restClient.post()
                    .uri(PATH)
                    .header("CL_AUTH_KEY", apiKey)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(Map.class);

            if (res == null) {
                throw new IllegalStateException("Apick 응답이 Null입니다.");
            }

            return res;
        } catch(Exception e) {
            log.error("[APICK][transfer1Won] fail accountNum={}, bankCode={}, bankName={}",
                    maskAccount(accountNum), bankCode, bankName, e);
            throw new RuntimeException("Apick 1원 송금 실패");
        }
    }

    private String maskAccount(String accountNum) {
        if (accountNum == null || accountNum.length() < 4) return "****";
        return "****" + accountNum.substring(accountNum.length() - 4);
    }
}
