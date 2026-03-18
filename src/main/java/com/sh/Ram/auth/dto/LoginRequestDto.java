package com.sh.Ram.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LoginRequestDto {

    private String name; // 이름
    private String email; // 이메일
    private String password; // 비밀번호
    private String nickName; // 별칭
    private String phone;
}