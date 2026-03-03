package be.intecbrussel.shoppinglist.service;

import be.intecbrussel.shoppinglist.dto.AdjustQuantityRequest;
import be.intecbrussel.shoppinglist.dto.StoredFoodRequest;
import be.intecbrussel.shoppinglist.dto.StoredFoodResponse;
import be.intecbrussel.shoppinglist.dto.StoredFoodUpdateRequest;

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
}
