package com.sh.Ram.batch.config;

import com.sh.Ram.auction.repository.AuctionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class AuctionEndBulkJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final AuctionRepository auctionRepository;

    @Bean
    public Job auctionEndBulkJob() {
        return new JobBuilder("auctionEndBulkJob", jobRepository)
                .start(auctionEndBulkStep())
                .build();
    }

    @Bean
    public Step auctionEndBulkStep() {
        return new StepBuilder("auctionEndBulkStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    long start = System.currentTimeMillis();

                    int updated = auctionRepository.bulkEndAuction();

                    long end = System.currentTimeMillis();

                    log.info("=== BULK DB TIME ===");
                    log.info("updated count = {}", updated);
                    log.info("execution time = {} ms", (end - start));

                    contribution.getStepExecution()
                            .getExecutionContext()
                            .putLong("dbTime", (end - start));

                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}
