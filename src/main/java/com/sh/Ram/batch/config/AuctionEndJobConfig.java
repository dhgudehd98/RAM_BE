package com.sh.Ram.batch.config;


import com.sh.Ram.entity.Auction;
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

@Configuration
@RequiredArgsConstructor
public class AuctionEndJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory emf;

    /**
     * 경매 마감 예약 배치 설정
     */

    @Bean
    public Job auctionEndJob() {
        System.out.println("=== auction end job 생성 ===");

        return new JobBuilder("auctionEndJob", jobRepository)
                .start(auctionEndStep())
                .build();
    }

    @Bean
    public Step auctionEndStep() {
        System.out.println("=== auction end step 생성 ===");

        return new StepBuilder("auctionEndStep", jobRepository)
                .<Auction, Auction>chunk(10, transactionManager)
                .reader(auctionEndReader())
                .processor(auctionEndProcessor())
                .writer(auctionEndWriter())
                .build();
    }

    @Bean
    public JpaPagingItemReader<Auction> auctionEndReader() {
        System.out.println("=== auctionEndReader 생성 ===");

        JpaPagingItemReader<Auction> reader = new JpaPagingItemReader<>();
        reader.setEntityManagerFactory(emf);

        reader.setQueryString(
                "SELECT a From Auction a " +
                        "WHERE a.auctionStatus = 'PROGRESS' " +
                        "AND a.endDate <= CURRENT_DATE"
        );

        reader.setPageSize(10);

        return reader;

    }

    @Bean
    public ItemProcessor<Auction, Auction> auctionEndProcessor() {

        return auction -> {
            System.out.println("=== Processor 실행 === auctionId=" + auction.getId()
                    + " status=" + auction.getAuctionStatus());

            auction.endAuction();

            System.out.println("=== Processor 변경 후 === auctionId=" + auction.getId()
                    + " status=" + auction.getAuctionStatus());

            return auction;
        };
    }

    @Bean
    public JpaItemWriter<Auction> auctionEndWriter() {

        JpaItemWriter<Auction> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(emf);

        writer.setUsePersist(false); // merge 사용

        return writer;
    }

}
