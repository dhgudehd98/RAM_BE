package com.sh.Ram;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ResaleAuctionMarketplaceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ResaleAuctionMarketplaceApplication.class, args);
	}

}
