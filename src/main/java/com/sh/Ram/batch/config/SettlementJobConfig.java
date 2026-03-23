package com.sh.Ram.batch.config;

import com.sh.Ram.auctionResult.repository.AuctionResultRepository;
import com.sh.Ram.auctionResult.service.AuctionResultService;
import com.sh.Ram.entity.AuctionResult;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class SettlementJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private final AuctionResultRepository auctionResultRepository;
    private final AuctionResultService auctionResultService;

    @Bean
    public Job settlementJob(EntityManagerFactory emf) {
        return new JobBuilder("settlementJob", jobRepository)
                .start(settlementStep(emf))
                .build();
    }

    @Bean
    public Step settlementStep(EntityManagerFactory emf) {
        return new StepBuilder("settlementStep", jobRepository)
                .<AuctionResult, AuctionResult>chunk(50, transactionManager)
                .reader(settlementReader(emf))
                .processor(settlementProcessor())
                .writer(settlementWriter(emf))
                .build();
    }

    @Bean
    public JpaPagingItemReader<AuctionResult> settlementReader(EntityManagerFactory emf) {

        JpaPagingItemReader<AuctionResult> reader = new JpaPagingItemReader<>();

        reader.setEntityManagerFactory(emf);
        reader.setPageSize(50);

        reader.setQueryString("""
                SELECT r FROM AuctionResult r
                WHERE r.settlementStatus = 'HELD'
                AND r.resultTime < :todayStart
                """);

        Map<String, Object> params = new HashMap<>();
        params.put("todayStart", LocalDate.now().atStartOfDay());

        reader.setParameterValues(params);

        return reader;
    }

    @Bean
    public ItemProcessor<AuctionResult, AuctionResult> settlementProcessor() {

        return result -> {

            Long sellerId = result.getAuction().getProduct().getMember().getId();
            Long price = Long.valueOf(result.getFinalPrice());

            /**
             * 출금 처리
             */
            auctionResultService.processOut(sellerId, price);

            /**
             *  판매자에게 입금 완료 시 상태 변경 (HELD -> SETTLED)
             */
            result.setSettlementStatus("SETTLED");

            return result;
        };
    }

    @Bean
    public JpaItemWriter<AuctionResult> settlementWriter(EntityManagerFactory emf) {

        JpaItemWriter<AuctionResult> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(emf);

        return writer;
    }
}
