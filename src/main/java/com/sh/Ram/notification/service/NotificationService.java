package com.sh.Ram.notification.service;

import com.sh.Ram.common.exception.member.MemberException;
import com.sh.Ram.entity.Member;
import com.sh.Ram.entity.Notification;
import com.sh.Ram.enums.NotificationType;
import com.sh.Ram.member.repository.MemberRepository;
import com.sh.Ram.notification.repository.NotificationRepository;
import com.sh.Ram.notification.repository.SseEmitRepository;
import com.sh.Ram.security.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository;
    private final SseEmitRepository sseEmitRepository;
    private final JwtUtil jwtUtil;
    public void send(Long memberId, String message, NotificationType type) {
        Member member = memberRepository.getReferenceById(memberId);
        if(member == null) throw new MemberException("존재 / 탈퇴한 회원입니다.");

        // 알림 메세지 저장
        notificationRepository.save(new Notification(member, message, type));
    }

    public SseEmitter subscribe(String token) {
        //토큰 검증
        if (!jwtUtil.validateToken(token)) {
            throw new IllegalArgumentException("유효하지 않는 토큰입니다.");
        }

        Long memberId = jwtUtil.getMemberId(token);
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);

        sseEmitRepository.save(memberId, emitter);

        // 아래와 같은 문제가 발생시 SSE 연결 제거
        emitter.onCompletion(() -> sseEmitRepository.deleteByMemberId(memberId)); // 정상적으로 연결 종료
        emitter.onTimeout(() -> sseEmitRepository.deleteByMemberId(memberId)); // 30분 타임 아웃
        emitter.onError(e -> sseEmitRepository.deleteByMemberId(memberId)); // 에러 발생


        // SSE 연결 후 , 임의의 데이터 전송
        // 데이터 전송 안하면 에러 발생
        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("연결 완료"));
        } catch (IOException e) {
            sseEmitRepository.deleteByMemberId(memberId);
        }

        return emitter;
    }
}