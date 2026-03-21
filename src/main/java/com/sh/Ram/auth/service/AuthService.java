package com.sh.Ram.auth.service;

import com.sh.Ram.auth.dto.LoginRefreshResponseDto;
import com.sh.Ram.auth.dto.LoginResponseDto;
import com.sh.Ram.common.exception.member.MemberException;
import com.sh.Ram.entity.Member;
import com.sh.Ram.auth.dto.LoginRequestDto;
import com.sh.Ram.auth.dto.MemberLoginRequestDto;
import com.sh.Ram.member.repository.MemberRepository;
import com.sh.Ram.redis.login.RedisLoginToken;
import com.sh.Ram.security.jwt.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.security.auth.message.AuthException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.Cookie;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
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

        // 로그인 후 , AccessToken에 대한 값 발급
        String accessToken = jwtUtil.generateAccessToken(member.getId(), member.getEmail());

        // 로그인 후 , RefreshToken에 대한 값 발급 및 Redis 저장
        String refreshToken = jwtUtil.generateRefreshToken(member.getId(), member.getEmail());
        redisLoginToken.setRefreshToken(refreshToken, member.getId());

        // Refresh
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true); // js 접근 불가
        cookie.setSecure(true); // HTTPS만 전송
        cookie.setPath("/");
        cookie.setMaxAge(86400);
        response.addCookie(cookie);

        return ResponseEntity.ok(
                new LoginResponseDto("Y", "정상적으로 로그인이 완료되었습니다.", accessToken, member.getNickname()));

    }

    public ResponseEntity<?> logout(Long memberId, HttpServletResponse response) throws AuthException {

        // Redis에 저장된 RefreshToken에 대한값이 있는지 확인
        redisLoginToken.getRefreshToken(memberId).orElseThrow(()-> new AuthException("RefreshToken에 대한 값이 존재하지 않습니다."));

        // Redis에 저장된 RefreshToken 값 삭제
        redisLoginToken.deleteRefreshToken(memberId);

        // 쿠키 삭제
        Cookie deleteCookie = new Cookie("refreshToken", null);
        deleteCookie.setMaxAge(0);
        deleteCookie.setPath("/");
        response.addCookie(deleteCookie);

        return ResponseEntity.ok(Map.of("result", "Y", "message", "정상적으로 로그아웃이 완료되었습니다."));
    }

    public ResponseEntity<?> refresh(HttpServletRequest request, HttpServletResponse response) throws AuthException {
        String refreshToken = getCookieValue(request, "refreshToken");
        LoginRefreshResponseDto refreshDto = refreshAccessToken(refreshToken);
        refreshDto.setResult("Y"); // accessToken에 대한 값이 정상적으로 재발급 되었을 때 result 값 설정

        return ResponseEntity.ok(refreshDto);
    }

    private LoginRefreshResponseDto refreshAccessToken(String refreshToken) throws AuthException {

        Claims claims;

        try {
            claims = jwtUtil.getClaims(refreshToken);
        } catch (ExpiredJwtException e) {
            throw new AuthException("세션이 만료되었습니다. 다시 로그인해주세요.");
        } catch (Exception e) {
            throw new AuthException("유효하지 않는 토큰입니다.");
        }


        // refreshToken에 대한 값을 바탕으로 memberId, email에 대한 정보 추출하기
        Long memberId = Long.parseLong(claims.getSubject());
        String email = claims.get("email", String.class);

        // Redis에 저장되어 있는 RefreshToken에 대한 값 가져오기
        String refreshTokenInRedis = redisLoginToken.getRefreshToken(memberId).orElseThrow(()->new AuthException("RefreshToken is InValid Token"));


        // 쿠키로 전달 받은 RefreshToken에 대한 값과 Redis에 저장되어 있는 RefreshToken에 대한 값이 불일치시 오류 발생
        if (!refreshToken.equals(refreshTokenInRedis)) throw new AuthException("토큰에 대한 정보가 일치하지 않습니다.");

        String accessToken = jwtUtil.generateAccessToken(memberId, email);
        String nickname = memberRepository.getReferenceById(memberId).getNickname();

        return new LoginRefreshResponseDto(accessToken, nickname);
    }


    private String getCookieValue(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            throw new IllegalArgumentException("쿠키가 존재하지 않습니다.");
        }

        for (Cookie cookie : cookies) {
            if(cookieName.equals(cookie.getName())) return cookie.getValue();
        }

        throw new IllegalArgumentException("쿠키를 찾을 수 없습니다.");
    }

    public boolean duplicationNickName(String nickName) {
        return memberRepository.existsByNickname(nickName);
    }

    public boolean duplicationEmail(String email) {
        return memberRepository.existsByEmail(email);
    }



}