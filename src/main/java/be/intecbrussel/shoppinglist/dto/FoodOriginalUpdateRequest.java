package be.intecbrussel.shoppinglist.dto;

import java.time.LocalDate;

/**
 * All fields are nullable — only non-null values are applied by the service.
 * This lets a caller update just the fields they care about (PATCH semantics).
 */
public record FoodOriginalUpdateRequest(
        String name,
        String remarks,
        LocalDate bestBeforeEnd,
        LocalDate useBy,
        Double original_ml_g,
        Double remaining_ml_g
) {}
