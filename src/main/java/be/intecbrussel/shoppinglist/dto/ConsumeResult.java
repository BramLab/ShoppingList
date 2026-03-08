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
     * The newly created StoredFood that represents the opened unit.
     * Null when the opened unit was immediately empty and therefore soft-deleted.
     */
    private Long openedStoredFoodId;

    /** Remaining ml/g in the opened unit (mirrors FoodOriginal.remaining_ml_g). */
    private double openedRemainingMlG;

    /** True when the opened unit was empty (remaining_ml_g ≤ 0) and has been soft-deleted. */
    private boolean openedUnitWasEmpty;
}
