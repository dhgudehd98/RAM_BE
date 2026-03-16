package com.sh.Ram.security.jwt;

import com.sh.Ram.redis.login.RedisLoginToken;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final RedisLoginToken redisLoginToken;

    // 요청 헤더에서 토큰 값 가져오기 -> Controller 진입하기전에 doFilterInternal에 저장
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);

        if (token != null && jwtUtil.validateToken(token)) {
            Long memberId = jwtUtil.getMemberId(token);
            String storedToken = redisLoginToken.getAccessToken(memberId);

            // 로그아웃이 된게 아니라면 token에 대한 값 return 받고, 로그아웃이 되었다면 memberId 자체에 대해 value 값이 존재하지 않기 때문에 null 값 반환
            if (storedToken != null) {
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                memberId,
                                null,
                                List.of(new SimpleGrantedAuthority("USER"))
                        );

                SecurityContextHolder.getContext().setAuthentication(authentication);

            }
        }

        filterChain.doFilter(request, response);
    }

    // 토큰에 대한 값이 Header로 온게 있는지 확인
    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}