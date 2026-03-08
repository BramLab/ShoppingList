package be.intecbrussel.shoppinglist.service;

import be.intecbrussel.shoppinglist.dto.AddToStorageRequest;
import be.intecbrussel.shoppinglist.dto.AddToStorageResult;

/**
 * Business operations on {@link be.intecbrussel.shoppinglist.model.FoodOriginal}.
 */
public interface FoodOriginalService {

    /**
     * Create a {@link be.intecbrussel.shoppinglist.model.FoodOriginal} and immediately
     * register it in storage as a {@link be.intecbrussel.shoppinglist.model.StoredFood}.
     *
     * <p>When {@code request.remainingMlG} is {@code null} it defaults to
     * {@code request.originalMlG} (sealed, nothing consumed yet).
     *
     * @param request all data needed for the FoodOriginal and its StoredFood wrapper.
     * @return identifiers and key fields of the two newly persisted rows.
     */
    AddToStorageResult addToStorage(AddToStorageRequest request);
}
