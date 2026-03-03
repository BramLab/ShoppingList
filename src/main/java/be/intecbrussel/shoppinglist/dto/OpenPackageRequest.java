package be.intecbrussel.shoppinglist.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDate;

/**
 * Used with FoodService#openPackage — marks a sealed product as opened,
 * setting a shorter useBy date and optionally recording an initial serving taken.
 */
public record OpenPackageRequest(
        @NotNull(message = "useBy date is required when opening a package")
        LocalDate useBy,

        @PositiveOrZero(message = "initialConsumption cannot be negative")
        double initialConsumption
) {}
