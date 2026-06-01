package com.sh.Ram.batch.config;

import com.sh.Ram.adminAccount.repository.AdminAccountRepository;
import com.sh.Ram.auction.repository.AuctionRepository;
import com.sh.Ram.auctionResult.repository.AuctionResultRepository;
import com.sh.Ram.auctionResult.service.AuctionResultService;
import com.sh.Ram.bid.repository.BidRepository;
import com.sh.Ram.common.exception.auctionResult.AuctionResultException;
import com.sh.Ram.entity.*;
import com.sh.Ram.product.repository.ProductRepository;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
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
    private final AdminAccountRepository adminAccountRepository;
    private final ProductRepository productRepository;

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
                .writer(auctionResultWriter())
                .build();
    }

    @Bean
    public JpaPagingItemReader<Auction> auctionResultReader(EntityManagerFactory emf) {

        JpaPagingItemReader<Auction> reader = new JpaPagingItemReader<>();

        reader.setEntityManagerFactory(emf);
        reader.setPageSize(50);

        reader.setQueryString("""
                SELECT a FROM Auction a
                JOIN FETCH a.product
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

                // Auction 연결
                result.setAuction(auction);

                /**
                 * 유찰
                 */
                if (topBidOpt.isEmpty()) {
                    result.setResultStatus("FAILED");
                    result.setSettlementStatus("NONE");
                    result.setBidCount(0);
                    result.setResultTime(LocalDateTime.now());

                    return result;
                }

                /**
                 * 낙찰
                 */
                Bid topBid = topBidOpt.get();

                Long buyerId = topBid.getMember().getId();

                result.setBuyerId(buyerId);
                result.setSellerId(result.getAuction().getProduct().getId()); // 판매자 아이디 등록
                result.setFinalPrice(topBid.getBidPrice());
                result.setBidCount((int) bidRepository.countByAuctionId(auction.getId()));
                result.setResultStatus("SUCCESS");
                result.setSettlementStatus("HELD");
                result.setAdminAccount(adminAccountRepository.findById(1L));

                result.setResultTime(LocalDateTime.now());

                return result;
            } catch (Exception e) {
                throw new AuctionResultException("AuctionResult 생성 중 오류 발생: " + auction.getId(), e);
            }
        };
    }

    @Bean
    public ItemWriter<AuctionResult> auctionResultWriter() {

        return items -> {
            for (AuctionResult result : items) {

                Auction auction = result.getAuction();

                if ("SUCCESS".equals(result.getResultStatus())) {
                    auctionResultService.processIn(
                            result.getBuyerId(),
                            Long.valueOf(result.getFinalPrice())
                    );
                }

                auctionResultRepository.save(result);

                Product product = auction.getProduct();
                product.setOnSale(true);

                productRepository.save(product);

            }
        };
    }
}
