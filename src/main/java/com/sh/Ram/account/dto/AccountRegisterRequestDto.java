package com.sh.Ram.account.dto;

import jakarta.validation.constraints.NotBlank;

public record AccountRegisterRequestDto(
        @NotBlank String accountNum,
        String bankCode,
        String bankName
) {}
