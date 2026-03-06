package com.sh.Ram.batch;

import com.sh.Ram.Auction.repository.AuctionRepository;
import com.sh.Ram.elasticSearch.product.repository.ProductDocumentRepository;
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
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@SpringBatchTest
public class AuctionEndBulkBatchTest {

    @Autowired
    JobLauncher jobLauncher;

    @Autowired
    Job auctionEndBulkJob;

    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private ProductRepository productRepository;

    @MockBean
    private ProductDocumentRepository productDocumentRepository;

    @BeforeEach
    void setUp() {
        auctionRepository.deleteAll();

        List<Product> products = productRepository.findAll();

        List<Auction> auctions = new ArrayList<>();

        for (int i = 0; i < 200; i++) {

            Product product = products.get(i % products.size());

            Auction auction = new Auction();
            auction.setProduct(product);
            auction.setAuctionStatus(AuctionStatus.PROGRESS);
            auction.setStartDate(LocalDate.now().minusDays(2));
            auction.setEndDate(LocalDate.now().minusDays(1));
            auction.setStartPrice(1000);
            auction.setCurrentPrice(2000);

            auctions.add(auction);
        }

        auctionRepository.saveAll(auctions);
    }

    @Test
    void 경매_종료_BULK_배치_성능_테스트() throws Exception {

        JobParameters params =
                new JobParametersBuilder()
                        .addLong("time", System.currentTimeMillis())
                        .toJobParameters();

        JobExecution execution =
                jobLauncher.run(auctionEndBulkJob, params);

        assertThat(execution.getStatus())
                .isEqualTo(BatchStatus.COMPLETED);

        // Batch 전체 시간
        Duration duration =
                Duration.between(
                        execution.getStartTime(),
                        execution.getEndTime()
                );

        long batchTime = duration.toMillis();

        // StepExecution
        StepExecution stepExecution =
                execution.getStepExecutions()
                        .iterator()
                        .next();

        long dbTime =
                stepExecution.getExecutionContext()
                        .getLong("dbTime", -1L);

        System.out.println("=== BULK BATCH PERFORMANCE ===");
        System.out.println("DB TIME : " + dbTime + " ms");
        System.out.println("BATCH TIME : " + batchTime + " ms");
    }
}
