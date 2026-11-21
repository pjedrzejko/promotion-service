package com.patrickjed.promotionservice;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionRepository repo;

    @GetMapping
    public List<Promotion> all() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public Promotion one(@PathVariable Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Promotion " + id + " not found"));
    }

    @GetMapping("/active")
    public List<Promotion> activeToday() {
        LocalDate today = LocalDate.now();
        return repo.findByActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(today, today);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Promotion create(@Valid @RequestBody Promotion promotion) {
        promotion.setId(null); // always create, never overwrite
        return repo.save(promotion);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        if (!repo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Promotion " + id + " not found");
        }
        repo.deleteById(id);
    }
}