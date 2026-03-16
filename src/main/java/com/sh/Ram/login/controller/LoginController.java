package com.sh.Ram.login.controller;

import com.sh.Ram.login.dto.LoginRequestDto;
import com.sh.Ram.login.dto.MemberLoginRequestDto;
import com.sh.Ram.login.service.LoginService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/login")
public class LoginController {
    private final LoginService loginService;

    // 로그인 페이지
    @GetMapping("")
    public String login() {
        return "login/login";
    }

    // 로그아웃
    @PostMapping("/logout")
    public void logout(Authentication authentication) {
        Long memberId = (Long) authentication.getPrincipal();
        loginService.logout(memberId);
    }

    // 로그인 - 비밀번호 / 아이디 유효성 검사
    @PostMapping("")
    @ResponseBody
    public ResponseEntity<?> login(
            @RequestBody MemberLoginRequestDto memberLoginRequestDto,
            HttpServletResponse response
    ) {
        return loginService.login(memberLoginRequestDto, response);
    }

    // 회원 가입 페이지
    @GetMapping("/join")
    public String join() {
        return "login/join";
    }

    // 회원가입
    @PostMapping("/join")
    @ResponseBody
    public Map<String, String> join(
            @RequestBody LoginRequestDto loginRequestDto
    ) {
        return loginService.join(loginRequestDto);
    }

    // 이메일 중복 확인
    @GetMapping("/duplication/email")
    @ResponseBody
    public boolean duplicationEmail(@RequestParam String email) {
        return loginService.duplicationEmail(email);
    }

    // 별칭 중복 확인
    @GetMapping("/duplication/nickName")
    @ResponseBody
    public boolean duplicationNickName(@RequestParam String nickName) {
        return loginService.duplicationNickName(nickName);
    }
}