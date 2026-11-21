package com.patrickjed.promotionservice;

import org.springframework.boot.SpringApplication;

public class TestPromotionServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(PromotionServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
