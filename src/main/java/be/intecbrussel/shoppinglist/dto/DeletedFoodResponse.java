package be.intecbrussel.shoppinglist.dto;

import java.time.LocalDate;
import java.util.Date;

/**
 * Response DTO for soft-deleted foods.
 * Extends the usual food fields with {@code updatedAt} (last-modified timestamp)
 * so the UI can show when the item was deleted / last touched.
 */
public record DeletedFoodResponse(
        long      id,
        String    name,
        String    remarks,
        LocalDate bestBeforeEnd,
        double    original_ml_g,
        LocalDate useBy,
        double    remaining_ml_g,
        LocalDate effectiveUseBy,
        boolean   empty,
        Date      updatedAt
) {}
