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

    /**
     * 경매 시작 배치
     * @throws Exception
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void runAuctionStartBatch() throws Exception {

        JobParameters params = new JobParametersBuilder()
                .addString("job", "auctionStartBulk")
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        log.info("Auction Start Bulk Batch 실행");

        jobLauncher.run(auctionStartBulkJob, params);
    }

    /**
     * 경매 종료 배치
     * @throws Exception
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void runAuctionEndBatch() throws Exception {

        JobParameters params = new JobParametersBuilder()
                .addString("job", "auctionEndBulk")
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        log.info("Auction End Bulk Batch 실행");

        jobLauncher.run(auctionEndBulkJob, params);
    }
}
