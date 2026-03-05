package com.sh.Ram.batch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class SchedulerConfig {

    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {

        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();

        scheduler.setPoolSize(4); // start, end 등 배치 동시 실행 보장
        scheduler.setThreadNamePrefix("auction-scheduler-");

        scheduler.initialize();

        return scheduler;
    }
}
