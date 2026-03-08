package be.intecbrussel.shoppinglist.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

/**
 * Used with FoodService#consume — record how much of a product was used.
 */
public record FoodOriginalConsumeRequest(
        @NotNull(message = "required when starting, can be updated after, or copy same if unchanged")
        LocalDate useBy,

        @Positive(message = "ml_g_left must be greater than 0")
        double ml_g_left
) {}
