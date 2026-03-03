package be.intecbrussel.shoppinglist.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDate;

/**
 * Used with FoodService#consume — record how much of a product was used.
 */
public record ConsumeRequest(
        @Positive(message = "amount must be greater than 0")
        double amount
) {}
