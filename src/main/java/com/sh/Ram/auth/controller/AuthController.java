package com.sh.Ram.auth.controller;

import com.sh.Ram.auth.dto.LoginRequestDto;
import com.sh.Ram.auth.dto.MemberLoginRequestDto;
import com.sh.Ram.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/auth")
@Slf4j
public class AuthController {
    private final AuthService authService;

    // 로그아웃
    @PostMapping("/logout")
    public void logout(Authentication authentication) {
        Long memberId = (Long) authentication.getPrincipal();
        authService.logout(memberId);
    }

    // 로그인 - 비밀번호 / 아이디 유효성 검사
    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<?> login(
            @RequestBody MemberLoginRequestDto memberLoginRequestDto,
            HttpServletResponse response
    ) {
        return authService.login(memberLoginRequestDto, response);
    }

    // 회원가입
    @PostMapping("/join")
    @ResponseBody
    public Map<String, String> join(
            @RequestBody LoginRequestDto loginRequestDto
    ) {
        return authService.join(loginRequestDto);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request, HttpServletResponse response) {
        return authService.refresh(request, response);
    }

    // 이메일 중복 확인
    @GetMapping("/duplication/email")
    @ResponseBody
    public boolean duplicationEmail(@RequestParam String email) {
        return authService.duplicationEmail(email);
    }

    // 별칭 중복 확인
    @GetMapping("/duplication/nickName")
    @ResponseBody
    public boolean duplicationNickName(@RequestParam String nickName) {
        return authService.duplicationNickName(nickName);
    }
}