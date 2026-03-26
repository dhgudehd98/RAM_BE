package com.sh.Ram.auction;

import com.sh.Ram.auction.dto.AuctionDto;
import com.sh.Ram.auction.service.AuctionService;
import com.sh.Ram.entity.Auction;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;

import java.util.List;

@SpringBootTest
public class AuctinoCursorTest {

    @Autowired
    private AuctionService auctionService;

    @Test
    @DisplayName("Cursor + QueryDSL을 활용한 전체 경매 조회  ")
    void findAllQueryDSL() {
        // 현재가 기준 내림 차순
        List<AuctionDto> auctions = auctionService.auctionList(null, null, null);

        Assertions.assertThat(auctions.size()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cursor + QueryDSL을 활용한 상태별 경매 조회 ")
    void findAuctionByStatus() {
        List<AuctionDto> auctionsProgress = auctionService.auctionList(null, null, "progress");
        Assertions.assertThat(auctionsProgress.size()).isEqualTo(2);

        List<AuctionDto> auctionsClosed = auctionService.auctionList(null, null, "closed");
        Assertions.assertThat(auctionsClosed.size()).isEqualTo(1);
    }


    @Test
    @DisplayName("Cursor + QueryDSL을 경매 상품 정렬 ")
    void findAuctionOrderBy() {
        //given - orderBy CurrentPrice
        List<AuctionDto> orderByCurrentPrice = auctionService.auctionList(null, "currentPrice", null);
        Assertions.assertThat(orderByCurrentPrice.get(0).getId()).isEqualTo(1035L);

        //given - order by StartDate
        List<AuctionDto> orderByStartDate = auctionService.auctionList(null, "startDate", null);
        Assertions.assertThat(orderByStartDate.get(1).getId()).isEqualTo(1036L);

        //given - order by endDate
        List<AuctionDto> orderByEndDate = auctionService.auctionList(null, "endDate", null);
        Assertions.assertThat(orderByEndDate.get(1).getId()).isEqualTo(1035L);

    }
}