package com.sh.Ram.login.service;

import com.sh.Ram.common.exception.member.MemberException;
import com.sh.Ram.entity.Member;
import com.sh.Ram.login.dto.LoginRequestDto;
import com.sh.Ram.login.dto.MemberLoginRequestDto;
import com.sh.Ram.member.repository.MemberRepository;
import com.sh.Ram.redis.login.RedisLoginToken;
import com.sh.Ram.security.jwt.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.Cookie;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class LoginService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisLoginToken redisLoginToken;
    private final JwtUtil jwtUtil;

//
    /**
     * 로그인 / 로그아웃 결과
     * - 이메일 : 이미 등록되어 있는지 안되어 있는지 확인 / 중복 불가 확인
     * - 비밀번호 : 영문 + 특수문자 + 숫자
     * - 닉네임 : 중복 불가 확인
     * @param loginRequestDto
     */
    @Transactional
    public Map<String,String> join(LoginRequestDto loginRequestDto) {

        String password = passwordEncoder.encode(loginRequestDto.getPassword());
        memberRepository.save(new Member(loginRequestDto, password));
        return Map.of("result", "Y", "msg", "회원가입이 정상적으로 완료되었습니다.");
    }
    public ResponseEntity<?> login(MemberLoginRequestDto memberLoginRequestDto, HttpServletResponse response) {
        // 여기서도 이메일에 대한 부분은 디코딩하고 설정을 해야됨.
        Member member = memberRepository.findByEmail(memberLoginRequestDto.getEmail()).orElseThrow(() -> new MemberException("이메일 정보가 올바르지 않습니다."));

        if (!passwordEncoder.matches(memberLoginRequestDto.getPassword(), member.getPassword())) {
            throw new MemberException("비밀번호가 올바르지 않습니다. 비밀번호를 확인해주세요.");
        }

        // 로그인 후, 토큰에 대한 값 Redis에 저장
        String token = jwtUtil.generateToken(member.getId(), member.getEmail());
        redisLoginToken.setAccessToken(token, member.getId());

        // AccessToken에 대한 값 Cookie에 저장
        Cookie cookie = new Cookie("accessToken", token);
        cookie.setHttpOnly(true); // js 접근 불가
        cookie.setSecure(true); // HTTPS만 전송
        cookie.setPath("/");
        cookie.setMaxAge(86400);
        response.addCookie(cookie);

        return ResponseEntity.ok(
                Map.of("result", "Y", "msg", "정상적으로 로그인이 완료되었습니다.", "token", token));
    }

    public boolean duplicationNickName(String nickName) {
        return memberRepository.existsByNickname(nickName);
    }

    public boolean duplicationEmail(String email) {
        return memberRepository.existsByEmail(email);
    }

    public void logout(Long memberId) {
        redisLoginToken.deleteAccessToken(memberId);
    }
}