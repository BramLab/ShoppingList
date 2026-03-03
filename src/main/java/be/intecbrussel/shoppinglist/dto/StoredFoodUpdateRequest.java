package be.intecbrussel.shoppinglist.dto;

import jakarta.validation.constraints.PositiveOrZero;

/**
 * Home and Food are immutable after creation.
 * Only storageType and quantity can be changed.
 */
public record StoredFoodUpdateRequest(
        Long storageTypeId,

        @PositiveOrZero(message = "quantity cannot be negative")
        Integer quantity
) {}
