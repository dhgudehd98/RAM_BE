package com.sh.Ram.login.service;

import com.sh.Ram.common.exception.member.MemberException;
import com.sh.Ram.entity.Member;
import com.sh.Ram.login.dto.LoginRequestDto;
import com.sh.Ram.login.dto.MemberLoginRequestDto;
import com.sh.Ram.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class LoginService {
    private final MemberRepository memberRepository;
//    private final BCryptPasswordEncoder passwordEncoder;
//
    /**
     * 로그인 / 로그아웃 결과
     * - 이메일 : 이미 등록되어 있는지 안되어 있는지 확인 / 중복 불가 확인
     * - 비밀번호 : 영문 + 특수문자 + 숫자
     * - 닉네임 : 중복 불가 확인
     * @param loginRequestDto
     */
    public Map<String,String> memberJoin(LoginRequestDto loginRequestDto) {

        // 비밀번호 -> Spring Security 도입시 암호화해서 저장
        memberRepository.save(new Member(loginRequestDto, loginRequestDto.getPassword()));
        return Map.of("result", "Y", "msg", "회원가입이 정상적으로 완료되었습니다.");
    }

    public boolean duplicationNickName(String nickName) {
        return memberRepository.existsByNickname(nickName);
    }

    public boolean duplicationEmail(String email) {
        return memberRepository.existsByEmail(email);
    }

    public Map<String, String> loginCheck(MemberLoginRequestDto memberLoginRequestDto) {
        // 여기서도 이메일에 대한 부분은 디코딩하고 설정을 해야됨.
        memberRepository.findByEmailAndPassword(memberLoginRequestDto.getEmail(), memberLoginRequestDto.getPassword()).orElseThrow(() -> new MemberException("이메일 또는 비밀번호가 올바르지 않습니다."));
        return Map.of("result", "Y", "msg", "정상적으로 로그인이 완료되었습니다.");
    }
}