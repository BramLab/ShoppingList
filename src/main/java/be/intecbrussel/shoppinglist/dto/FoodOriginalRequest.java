package be.intecbrussel.shoppinglist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record FoodOriginalRequest(
        @NotBlank(message = "Food name is required")
        String name,

        String remarks,

        @NotNull(message = "bestBeforeEnd date is required")
        LocalDate bestBeforeEnd,

        @Positive(message = "original_ml_g must be greater than 0")
        double original_ml_g
) {}
