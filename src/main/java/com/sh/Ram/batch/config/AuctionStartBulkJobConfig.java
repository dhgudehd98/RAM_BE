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
public class AuctionStartBulkJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;
    private final AuctionRepository auctionRepository;

    @Bean
    public Job auctionStartBulkJob() {
        return new JobBuilder("auctionStartBulkJob", jobRepository)
                .start(auctionStartBulkStep())
                .build();
    }

    @Bean
    public Step auctionStartBulkStep() {
        return new StepBuilder("auctionStartBulkStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {

                    long start = System.currentTimeMillis();

                    int updated = auctionRepository.bulkStartAuction();

                    long end = System.currentTimeMillis();

                    log.info("=== BULK START DB TIME ===");
                    log.info("updated count = {}", updated);
                    log.info("execution time = {} ms", (end - start));

                    contribution.getStepExecution()
                            .getExecutionContext()
                            .putLong("dbTime", (end - start));

                    return RepeatStatus.FINISHED;

                }, platformTransactionManager)
                .build();
    }
}
