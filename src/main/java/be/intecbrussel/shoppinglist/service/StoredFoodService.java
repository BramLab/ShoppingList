package be.intecbrussel.shoppinglist.service;

import be.intecbrussel.shoppinglist.dto.*;

import java.util.List;

public interface StoredFoodService {
    StoredFoodResponse saveStoredFood(StoredFoodRequest request);
    List<StoredFoodResponse> findAllStoredFoods();
    List<StoredFoodResponse> findAllByHomeId(long homeId);
    StoredFoodResponse findStoredFoodById(long id);
    StoredFoodResponse updateStoredFood(long id, StoredFoodUpdateRequest request);
    void deleteStoredFood(long id);

    /** Increase or decrease quantity by delta (negative = consume from stock).
     *  Deletes the entry automatically when quantity reaches 0. */
    StoredFoodResponse adjustQuantity(long id, AdjustQuantityRequest request);


    /**
     * Take one unit out of a stored pack.
     *
     * <p>Steps performed:
     * <ol>
     *   <li>Decrement {@code StoredFood.quantity} by 1.  When quantity reaches 0 the
     *       source {@code StoredFood} row is deleted.</li>
     *   <li>Create a brand-new {@code FoodOriginal} that copies {@code name} and
     *       {@code remarks} from the base {@code Food}, and stores the provided
     *       {@code remainingMlG} / {@code useBy} / {@code bestBeforeEnd}.</li>
     *   <li>Wrap that {@code FoodOriginal} in a new {@code StoredFood} (quantity = 1)
     *       linked to the same {@code Home}.  The caller may override the
     *       {@code StorageType} via {@link ConsumeRequest#getStorageTypeId()}.</li>
     *   <li>If {@code remainingMlG ≤ 0} the opened unit is immediately soft-deleted
     *       and no {@code StoredFood} row is created for it.</li>
     * </ol>
     *
     * @param storedFoodId ID of the source {@code StoredFood} (the sealed pack).
     * @param request      Details about the opened unit.
     * @return             Summary of what changed.
     */
    ConsumeResult consume(long storedFoodId, ConsumeRequest request);
}
