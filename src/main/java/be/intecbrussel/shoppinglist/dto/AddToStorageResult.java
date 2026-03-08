package be.intecbrussel.shoppinglist.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Summary returned after a successful {@code addToStorage} call.
 */
@Data
@Builder
public class AddToStorageResult {

    /** The persisted {@link be.intecbrussel.shoppinglist.model.FoodOriginal} id. */
    private long foodOriginalId;

    /** The persisted {@link be.intecbrussel.shoppinglist.model.StoredFood} id. */
    private long storedFoodId;

    /** Quantity that was stored (mirrors {@link AddToStorageRequest#getQuantity()}). */
    private int quantity;

    /** Remaining ml/g as actually stored (may differ when the request omitted the field). */
    private double remainingMlG;
}
