package com.patrickjed.promotionservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static com.patrickjed.promotionservice.Promotion.DiscountType.PERCENT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PromotionController.class)
class PromotionControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    PromotionRepository repo;

    @Test
    void getById_returnsPromotion() throws Exception {
        Promotion p = Promotion.builder()
                .id(1L)
                .name("Black Friday")
                .discountType(PERCENT)
                .discountValue(new BigDecimal("20"))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(5))
                .active(true)
                .build();
        when(repo.findById(1L)).thenReturn(Optional.of(p));

        mvc.perform(get("/promotions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Black Friday"))
                .andExpect(jsonPath("$.discountType").value("PERCENT"));
    }

    @Test
    void getById_returns404WhenMissing() throws Exception {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        mvc.perform(get("/promotions/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_rejectsInvalidPromotion() throws Exception {
        String badJson = """
                {"name":"","discountType":"PERCENT","discountValue":-5,
                 "startDate":"2026-11-21","endDate":"2026-12-01","active":true}
                """;

        mvc.perform(post("/promotions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badJson))
                .andExpect(status().isBadRequest());

        verify(repo, never()).save(any());
    }
}
