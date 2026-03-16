package com.sh.Ram.login.controller;

import com.sh.Ram.login.dto.LoginRequestDto;
import com.sh.Ram.login.dto.MemberLoginRequestDto;
import com.sh.Ram.login.service.LoginService;
import lombok.RequiredArgsConstructor;
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

    // 로그인 확인
    @PostMapping("/check")
    @ResponseBody
    public Map<String, String> check(
            @RequestBody MemberLoginRequestDto memberLoginRequestDto
    ) {
        return loginService.loginCheck(memberLoginRequestDto);
    }

    // 회원 가입 페이지
    @GetMapping("/join")
    public String join() {
        return "login/join";
    }

    // 회원가입
    @PostMapping("/join")
    @ResponseBody
    public Map<String, String> memberJoin(
            @RequestBody LoginRequestDto loginRequestDto
    ) {
        return loginService.memberJoin(loginRequestDto);
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