package com.sh.Ram.apick.dto;

import jakarta.validation.constraints.NotBlank;

public record ApickVerifyRequest(
        @NotBlank String code
) {}
