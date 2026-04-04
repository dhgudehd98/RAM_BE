package com.sh.Ram.apick.controller;

import com.sh.Ram.apick.dto.ApickSendRequest;
import com.sh.Ram.apick.dto.ApickVerifyRequest;
import com.sh.Ram.apick.service.ApickService;
import com.sh.Ram.common.dto.SimpleMessageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/apick")
public class ApickController {

    private final ApickService apickService;

    @PostMapping("/send")
    public ResponseEntity<SimpleMessageResponse> send(
            Authentication authentication,
            @Valid @RequestBody ApickSendRequest req) {
        Long memberId = (Long) authentication.getPrincipal();

        apickService.sendCode(memberId, req);
        return ResponseEntity.ok(
                new SimpleMessageResponse("1원 인증 요청 완료. 통장 입금 메모의 뒤 4자리 숫자를 입력해주세요.")
        );
    }

    @PostMapping("/verify")
    public ResponseEntity<SimpleMessageResponse> verify(
            Authentication authentication,
            @Valid @RequestBody ApickVerifyRequest req) {
        Long memberId = (Long) authentication.getPrincipal();

        apickService.verifyCode(memberId, req);

        return ResponseEntity.ok(
                new SimpleMessageResponse("계좌 인증이 완료되었습니다.")
        );
    }
}
