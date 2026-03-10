package be.intecbrussel.shoppinglist.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Summary returned by {@link be.intecbrussel.shoppinglist.service.StoredFoodService#consume}.
 */
@Data
@Builder
public class ConsumeResult {

    /** The source StoredFood after its quantity was decremented (null when it was the last unit and is now deleted). */
    private Long sourceStoredFoodId;

    /** Remaining quantity in the source pack (0 = the source StoredFood was deleted). */
    private int sourceRemainingQuantity;

    /**
     * The newly created StoredFood that represents the opened units.
     * Null when the opened units were immediately empty and therefore soft-deleted.
     */
    private Long openedStoredFoodId;

    /** How many units were consumed (mirrors ConsumeRequest.quantity). */
    private int consumedQuantity;

    /** Remaining ml/g in each opened unit (mirrors FoodOriginal.remaining_ml_g). */
    private double openedRemainingMlG;

    /** True when the opened units were empty (remaining_ml_g ≤ 0) and have been soft-deleted. */
    private boolean openedUnitWasEmpty;
}