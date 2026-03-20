package com.sh.Ram.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoginRefreshResponseDto {
    private String result;
    private String token;
    private String nickname;

    @Override
    public String toString() {
        return "LoginRefreshResponseDto{" +
                "result='" + result + '\'' +
                ", accessToken='" + token + '\'' +
                ", nickname='" + nickname + '\'' +
                '}';
    }

    public LoginRefreshResponseDto(String accessToken, String nickname) {
        this.token = accessToken;
        this.nickname = nickname;
    }
}