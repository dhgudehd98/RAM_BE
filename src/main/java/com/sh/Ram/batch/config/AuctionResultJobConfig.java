package com.sh.Ram.batch.config;

import com.sh.Ram.auction.repository.AuctionRepository;
import com.sh.Ram.auctionResult.repository.AuctionResultRepository;
import com.sh.Ram.auctionResult.service.AuctionResultService;
import com.sh.Ram.bid.repository.BidRepository;
import com.sh.Ram.common.exception.auctionResult.AuctionResultException;
import com.sh.Ram.entity.*;
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

import java.time.LocalDateTime;
import java.util.Optional;

@Configuration
@RequiredArgsConstructor
public class AuctionResultJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final AuctionResultRepository auctionResultRepository;
    private final AuctionResultService auctionResultService;

    @Bean
    public Job auctionResultJob(EntityManagerFactory emf) {
        return new JobBuilder("auctionResultJob", jobRepository)
                .start(auctionResultStep(emf))
                .build();
    }

    @Bean
    public Step auctionResultStep(EntityManagerFactory emf) {
        return new StepBuilder("auctionResultStep", jobRepository)
                .<Auction, AuctionResult> chunk(50, transactionManager)
                .reader(auctionResultReader(emf))
                .processor(auctionResultProcessor())
                .writer(auctionResultWriter(emf))
                .build();
    }

    @Bean
    public JpaPagingItemReader<Auction> auctionResultReader(EntityManagerFactory emf) {

        JpaPagingItemReader<Auction> reader = new JpaPagingItemReader<>();

        reader.setEntityManagerFactory(emf);
        reader.setPageSize(50);

        reader.setQueryString("""
                SELECT a FROM Auction a
                WHERE a.auctionStatus = 'CLOSED'
                AND a.auctionResult IS NULL
                """);

        return reader;
    }

    @Bean
    public ItemProcessor<Auction, AuctionResult> auctionResultProcessor() {

        return auction -> {

            try {
                Optional<Bid> topBidOpt =
                        bidRepository.findTopByAuctionIdOrderByBidPriceDesc(auction.getId());

                AuctionResult result = new AuctionResult();

                /**
                 * 유찰
                 */
                if (topBidOpt.isEmpty()) {
                    result.setResultStatus("FAILED");
                    result.setSettlementStatus("NONE");
                    result.setBidCount(0);
                    result.setResultTime(LocalDateTime.now());

                    auction.setAuctionResult(result);

                    return result;
                }

                /**
                 * 낙찰
                 */
                Bid topBid = topBidOpt.get();

                Long buyerId = topBid.getMember().getId();
                Long price = Long.valueOf(topBid.getBidPrice());

                /**
                 * 입금 처리
                 */
                auctionResultService.processIn(buyerId, price);

                result.setBuyerId(buyerId);
                result.setFinalPrice(topBid.getBidPrice());
                result.setBidCount((int) bidRepository.countByAuctionId(auction.getId()));
                result.setResultStatus("SUCCESS");
                result.setSettlementStatus("HELD");

                result.setResultTime(LocalDateTime.now());

                auction.setAuctionResult(result);

                return result;
            } catch (Exception e) {
                throw new AuctionResultException("AuctionResult 생성 중 오류 발생: " + auction.getId(), e);
            }
        };
    }

    @Bean
    public JpaItemWriter<AuctionResult> auctionResultWriter(EntityManagerFactory emf) {

        JpaItemWriter<AuctionResult> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(emf);

        return writer;
    }
}
