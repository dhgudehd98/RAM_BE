package com.sh.Ram.batch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuctionBatchScheduler {

    private final JobLauncher jobLauncher;

    private final Job auctionStartBulkJob;
    private final Job auctionEndBulkJob;
    private final Job auctionResultJob;
    private final Job settlementJob;

    /**
     * 경매 시작 배치
     * @throws Exception
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void runAuctionStartBatch() throws Exception {

        log.info("Auction Start Bulk Batch 실행");

        jobLauncher.run(auctionStartBulkJob, createParams("auctionStartBulk"));
    }

    /**
     * 경매 종료 배치
     * @throws Exception
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void runAuctionEndBatch() throws Exception {

        log.info("Auction End Bulk Batch 실행");

        jobLauncher.run(auctionEndBulkJob, createParams("auctionEndBulk"));
    }

    /**
     * 경매 결과 배치 (낙찰 시 입금 처리 동시 진행)
     * 매일 00시 05분
     */
    @Scheduled(cron = "0 5 0 * * *")
    public void runAuctionResultBatch() throws Exception {

        log.info("Auction Result Batch 실행");

        jobLauncher.run(auctionResultJob, createParams("auctionResult"));
    }

    /**
     * 출금 처리 배치
     * 매일 00시 10분
     * @throws Exception
     */
    @Scheduled(cron = "0 10 0 * * *")
    public void runSettlementBatch() throws Exception {

        log.info("Settlement Batch 실행");

        jobLauncher.run(settlementJob, createParams("settlement"));
    }

    /**
     * 공통 메서드 분리
     */
    private JobParameters createParams(String jobName) {
        return new JobParametersBuilder()
                .addString("job", jobName)
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();
    }
}
