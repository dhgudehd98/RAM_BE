package com.sh.Ram.apick.dto;

import jakarta.validation.constraints.NotBlank;

public record ApickSendRequest(
        @NotBlank String accountNum,
        String bankCode,
        String bankName
) {}
