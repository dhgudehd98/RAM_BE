package com.sh.Ram.accountHistory.controller;

import com.sh.Ram.accountHistory.dto.AccountHistoryDto;
import com.sh.Ram.accountHistory.service.AccountHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/account")
public class AccountHistoryController {

    private final AccountHistoryService accountHistoryService;

    @GetMapping("/history")
    public Page<AccountHistoryDto> getHistory(
            Authentication authentication,
            @RequestParam(required = false) String type,
            @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {
        Long memberId = (Long) authentication.getPrincipal();
        return accountHistoryService.getHistory(memberId, type, pageable);
    }
}
