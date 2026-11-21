package com.patrickjed.promotionservice;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static com.patrickjed.promotionservice.Promotion.DiscountType.AMOUNT;
import static com.patrickjed.promotionservice.Promotion.DiscountType.PERCENT;

@Configuration
public class SeedData {

    @Bean
    CommandLineRunner seed(PromotionRepository repo) {
        return args -> {
            if (repo.count() > 0) return; // don't duplicate on a persistent DB

            LocalDate t = LocalDate.now();
            repo.saveAll(List.of(
                    Promotion.builder()
                            .name("Black Friday Tools 20% Off")
                            .discountType(PERCENT)
                            .discountValue(new BigDecimal("20"))
                            .startDate(t.minusDays(2))
                            .endDate(t.plusDays(5))
                            .active(true)
                            .build(),
                    Promotion.builder()
                            .name("$10 Off Paint Gallon")
                            .discountType(AMOUNT)
                            .discountValue(new BigDecimal("10"))
                            .startDate(t)
                            .endDate(t.plusDays(14))
                            .active(true)
                            .build(),
                    Promotion.builder()
                            .name("Spring Garden Sale")
                            .discountType(PERCENT)
                            .discountValue(new BigDecimal("15"))
                            .startDate(t.plusMonths(4))
                            .endDate(t.plusMonths(5))
                            .active(true)
                            .build()
            ));
        };
    }
}