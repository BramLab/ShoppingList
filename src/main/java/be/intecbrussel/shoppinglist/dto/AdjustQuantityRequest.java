package be.intecbrussel.shoppinglist.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Used with StoredFoodService#adjustQuantity.
 * delta > 0 means restock, delta < 0 means consume from stock.
 */
public record AdjustQuantityRequest(
        @NotNull(message = "delta is required")
        int delta
) {}
