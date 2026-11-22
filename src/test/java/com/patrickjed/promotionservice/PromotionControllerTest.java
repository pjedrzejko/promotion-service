package com.patrickjed.promotionservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web slice test: starts only the MVC layer for PromotionController,
 * plus our SecurityConfig so the real access rules apply.
 * The repository is a Mockito mock, so no database is used.
 */
@WebMvcTest(PromotionController.class)
@Import(SecurityConfig.class)
class PromotionControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    PromotionRepository repo;

    private static final String VALID_JSON = """
            {"name":"Holiday Lights","discountType":"PERCENT","discountValue":25,
             "startDate":"2026-12-01","endDate":"2026-12-24","active":true}
            """;

    private Promotion samplePromotion() {
        return Promotion.builder()
                .id(1L)
                .name("Black Friday")
                .discountType(PERCENT)
                .discountValue(new BigDecimal("20"))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(5))
                .active(true)
                .build();
    }

    // ---------- reads are public ----------

    @Test
    void getById_returnsPromotion_withoutToken() throws Exception {
        when(repo.findById(1L)).thenReturn(Optional.of(samplePromotion()));

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

    // ---------- writes need ADMIN ----------

    @Test
    void create_withoutToken_returns401() throws Exception {
        mvc.perform(post("/promotions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_JSON))
                .andExpect(status().isUnauthorized());

        verify(repo, never()).save(any());
    }

    @Test
    void create_withNonAdminToken_returns403() throws Exception {
        mvc.perform(post("/promotions")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_JSON))
                .andExpect(status().isForbidden());

        verify(repo, never()).save(any());
    }

    @Test
    void create_withAdminToken_returns201() throws Exception {
        when(repo.save(any())).thenReturn(samplePromotion());

        mvc.perform(post("/promotions")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    void create_rejectsInvalidPromotion() throws Exception {
        // blank name and negative discount should fail @Valid
        String badJson = """
                {"name":"","discountType":"PERCENT","discountValue":-5,
                 "startDate":"2026-11-21","endDate":"2026-12-01","active":true}
                """;

        mvc.perform(post("/promotions")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badJson))
                .andExpect(status().isBadRequest());

        verify(repo, never()).save(any());
    }

    @Test
    void delete_withoutToken_returns401() throws Exception {
        mvc.perform(delete("/promotions/1"))
                .andExpect(status().isUnauthorized());

        verify(repo, never()).deleteById(any());
    }
}
