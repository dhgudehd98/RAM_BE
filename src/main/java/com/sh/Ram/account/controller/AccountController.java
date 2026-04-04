package com.sh.Ram.account.controller;

import com.sh.Ram.account.dto.AccountDto;
import com.sh.Ram.account.dto.AccountRegisterRequestDto;
import com.sh.Ram.account.dto.AccountRequestDto;
import com.sh.Ram.account.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/account")
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/my")
    @ResponseBody
    public AccountDto getAccount(
            Authentication authentication
    ) {
        Long memberId = (Long) authentication.getPrincipal();

        return accountService.getAccount(memberId);
    }

    @PostMapping("/charge")
    @ResponseBody
    public Map<String, String> chargeAccount(
            @RequestBody AccountRequestDto req,
            Authentication authentication
    ) {
        Long memberId = (Long) authentication.getPrincipal();

        return accountService.chargeAccount(memberId, req);
    }

    @PostMapping("/register")
    public Map<String, String> registerAccount(
            Authentication authentication,
            @Valid @RequestBody AccountRegisterRequestDto req
    ) {
        Long memberId = (Long) authentication.getPrincipal();

        return accountService.registerAccount(memberId, req);
    }

    @DeleteMapping("/delete")
    public Map<String, String> deleteAccount(Authentication authentication) {

        Long memberId = (Long) authentication.getPrincipal();

        return accountService.deleteAccount(memberId);
    }
}
