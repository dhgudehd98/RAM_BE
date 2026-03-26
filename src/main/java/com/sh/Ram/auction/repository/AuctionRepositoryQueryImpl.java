package com.sh.Ram.auction.repository;


import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sh.Ram.auction.dto.AuctionDto;
import com.sh.Ram.entity.Auction;
import com.sh.Ram.entity.QAuction;
import com.sh.Ram.enums.AuctionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.sh.Ram.entity.QAuction.auction;

@RequiredArgsConstructor
@Repository
public class AuctionRepositoryQueryImpl implements AuctionRepositoryQuery {

    private final JPAQueryFactory jpaQueryFactory;
    @Override
    public List<Auction> findAll(Long lastId, String sort, String status) {
        return jpaQueryFactory
                .selectFrom(auction)
                .join(auction.product).fetchJoin()
                .join(auction.product.brand).fetchJoin()
                .leftJoin(auction.auctionResult).fetchJoin()
                .where(
                        status != null ? auction.auctionStatus.eq(AuctionStatus.valueOf(status.toUpperCase())) : null,
                        lastId != null ? auction.id.lt(lastId) : null
                )
                .orderBy(
                        getOrderBy(sort)
                )
                .limit(10)
                .fetch();
    }

    private OrderSpecifier<?> getOrderBy(String sort) {
        if(sort == null) return auction.id.desc();

        return switch (sort) {
            case "currentPrice" -> auction.currentPrice.desc();
            case "startDate" -> auction.startDate.desc();
            case "endDate" -> auction.endDate.desc();
            default -> auction.id.desc();
        };
    }
}