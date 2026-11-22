package com.patrickjed.promotionservice;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    // Spring Data builds the SQL from the method name:
    // WHERE active = true AND start_date <= ? AND end_date >= ?
    List<Promotion> findByActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            LocalDate startOnOrBefore, LocalDate endOnOrAfter);
}


