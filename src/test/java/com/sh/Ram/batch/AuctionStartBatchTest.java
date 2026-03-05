package com.sh.Ram.batch;


import com.sh.Ram.Auction.repository.AuctionRepository;
import com.sh.Ram.entity.Auction;
import com.sh.Ram.entity.Product;
import com.sh.Ram.enums.AuctionStatus;
import com.sh.Ram.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@SpringBatchTest
public class AuctionStartBatchTest {

    @Autowired
    JobLauncher jobLauncher;

    @Autowired
    Job auctionStartJob;

    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private ProductRepository productRepository;

    private Auction savedAuction;

    @BeforeEach
    void setUp() {

        auctionRepository.deleteAll();

        // 기존 Product 중 하나 가져오기
        Product product = productRepository.findAll().get(0);

        // Auction 생성
        Auction auction = new Auction();
        auction.setProduct(product);
        auction.setAuctionStatus(AuctionStatus.PENDING);
        auction.setStartDate(LocalDate.now().minusDays(1));
        auction.setEndDate(LocalDate.now().plusDays(1));
        auction.setStartPrice(1000);
        auction.setCurrentPrice(1000);

        savedAuction = auctionRepository.save(auction);
    }

    @Test
    void 경매_시작_배치_정상동작() throws Exception {
        // given
        JobParameters params =
                new JobParametersBuilder()
                        .addLong("time", System.currentTimeMillis())
                        .toJobParameters();

        // when
        JobExecution execution =
                jobLauncher.run(auctionStartJob, params);

        // then
        assertThat(execution.getStatus())
                .isEqualTo(BatchStatus.COMPLETED);

        Auction result =
                auctionRepository.findById(savedAuction.getId()).orElseThrow();

        assertThat(result.getAuctionStatus())
                .isEqualTo(AuctionStatus.PROGRESS);
    }
}
