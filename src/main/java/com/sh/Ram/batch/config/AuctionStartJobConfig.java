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
public class AuctionStartJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory emf;

    /**
     * 경매 시작 예약 배치 기능 설정
     */

    @Bean
    public Job auctionStartJob() {
        System.out.println("=== auction start job 생성 ===");

        return new JobBuilder("auctionStartJob", jobRepository)
                .start(auctionStartStep())
                .build();
    }

    @Bean
    public Step auctionStartStep() {
        System.out.println("=== auction start step 생성 ===");

        return new StepBuilder("auctionStartStep", jobRepository)
                .<Auction, Auction>chunk(10, transactionManager)
                .reader(auctionStartReader())
                .processor(auctionStartProcessor())
                .writer(auctionStartWriter())
                .build();
    }

    @Bean
    public JpaPagingItemReader<Auction> auctionStartReader() {

        System.out.println("=== auctionStartReader 생성 ===");

        JpaPagingItemReader<Auction> reader = new JpaPagingItemReader<>();
        reader.setEntityManagerFactory(emf);

        reader.setQueryString(
                "SELECT a FROM Auction a " +
                        "WHERE a.auctionStatus = 'PENDING' " +
                        "AND a.startDate <= CURRENT_DATE"
        );

        reader.setPageSize(10);

        return reader;
    }

    @Bean
    public ItemProcessor<Auction, Auction> auctionStartProcessor() {

        return auction -> {
            System.out.println("=== Processor 실행 === auctionId=" + auction.getId()
                    + " status=" + auction.getAuctionStatus());

            auction.startAuction();

            System.out.println("=== Processor 변경 후 === auctionId=" + auction.getId()
                    + " status=" + auction.getAuctionStatus());

            return auction;
        };
    }

    @Bean
    public JpaItemWriter<Auction> auctionStartWriter() {

        JpaItemWriter<Auction> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(emf);

        writer.setUsePersist(false); // merge 사용

        return writer;
    }
}
