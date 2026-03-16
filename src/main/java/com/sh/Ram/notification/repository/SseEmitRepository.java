package com.sh.Ram.notification.repository;


import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class SseEmitRepository {
    private final Map<Long, SseEmitter> emitterMap = new ConcurrentHashMap<>();

    public SseEmitter save(Long memberId, SseEmitter emitter) {
        emitterMap.put(memberId, emitter);

        return emitter;
    }

    public void deleteByMemberId(Long memberId) {
        emitterMap.remove(memberId);
    }

    public SseEmitter findByMemberId(Long memberId, SseEmitter emitter) {
        return emitterMap.get(memberId);
    }

}