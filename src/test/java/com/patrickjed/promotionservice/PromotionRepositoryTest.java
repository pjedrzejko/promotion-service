package com.patrickjed.promotionservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static com.patrickjed.promotionservice.Promotion.DiscountType.PERCENT;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PromotionRepositoryTest {

    @Autowired
    PromotionRepository repo;

    private Promotion promo(String name, LocalDate start, LocalDate end, boolean active) {
        return Promotion.builder()
                .name(name)
                .discountType(PERCENT)
                .discountValue(new BigDecimal("10"))
                .startDate(start)
                .endDate(end)
                .active(active)
                .build();
    }

    @Test
    void savesAndReadsBackAPromotion() {
        LocalDate today = LocalDate.now();
        Promotion saved = repo.save(promo("Test Promo", today, today.plusDays(7), true));

        assertThat(saved.getId()).isNotNull();
        assertThat(repo.findById(saved.getId()))
                .isPresent()
                .get()
                .extracting(Promotion::getName)
                .isEqualTo("Test Promo");
    }

    @Test
    void findsOnlyPromotionsActiveToday() {
        LocalDate today = LocalDate.now();
        repo.saveAll(List.of(
                promo("Running now", today.minusDays(1), today.plusDays(1), true),
                promo("Starts today", today, today.plusDays(3), true),
                promo("Ended yesterday", today.minusDays(5), today.minusDays(1), true),
                promo("Future", today.plusDays(10), today.plusDays(20), true),
                promo("Switched off", today.minusDays(1), today.plusDays(1), false)
        ));

        List<Promotion> active = repo
                .findByActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(today, today);

        assertThat(active)
                .extracting(Promotion::getName)
                .containsExactlyInAnyOrder("Running now", "Starts today");
    }
}
