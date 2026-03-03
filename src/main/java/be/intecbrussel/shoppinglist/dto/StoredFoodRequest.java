package be.intecbrussel.shoppinglist.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record StoredFoodRequest(
        @NotNull(message = "homeId is required")
        Long homeId,

        @NotNull(message = "foodId is required")
        Long foodId,

        @NotNull(message = "storageTypeId is required")
        Long storageTypeId,

        @PositiveOrZero(message = "quantity cannot be negative")
        int quantity
) {}
