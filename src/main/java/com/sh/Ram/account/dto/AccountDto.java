package com.sh.Ram.account.dto;

import lombok.Getter;

@Getter
public class AccountDto {

    private Long accountId;
    private String bankName;
    private String bankCode;
    private String accountNum;
    private Long accountBalance;

    public AccountDto(Long accountId, String bankName, String bankCode, String accountNum, Long accountBalance) {
        this.accountId = accountId;
        this.bankName = bankName;
        this.bankCode = bankCode;
        this.accountNum = accountNum;
        this.accountBalance = accountBalance;
    }
}
