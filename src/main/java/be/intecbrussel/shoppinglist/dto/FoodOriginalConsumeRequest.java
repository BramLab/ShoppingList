package be.intecbrussel.shoppinglist.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDate;

/**
 * Used with FoodService#openPackage — marks a sealed product as opened,
 * setting a shorter useBy date and optionally recording an initial serving taken.
 */
public record FoodOriginalConsumeRequest (
        @NotNull(message = "required when starting, can be updated after")
        LocalDate useBy,

        @PositiveOrZero(message = "consumption cannot be negative")
        double ml_g_consumed
) {
}
