package com.sh.Ram.notification.service;

import com.sh.Ram.common.exception.member.MemberException;
import com.sh.Ram.entity.Member;
import com.sh.Ram.entity.Notification;
import com.sh.Ram.enums.NotificationType;
import com.sh.Ram.member.repository.MemberRepository;
import com.sh.Ram.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository;
    public void send(Long memberId, String message, NotificationType type) {
        Member member = memberRepository.getReferenceById(memberId);
        if(member == null) throw new MemberException("존재 / 탈퇴한 회원입니다.");

        // 알림 메세지 저장
        notificationRepository.save(new Notification(member, message, type));
    }
}