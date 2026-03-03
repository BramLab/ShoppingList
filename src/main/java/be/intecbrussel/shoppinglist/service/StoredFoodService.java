package be.intecbrussel.shoppinglist.service;

import be.intecbrussel.shoppinglist.model.StoredFood;

import java.util.List;

public interface StoredFoodService {
    StoredFood saveStoredFood(StoredFood storedFood);
    List<StoredFood> findAllStoredFoods();
    List<StoredFood> findAllByHomeId(long homeId);
    StoredFood findStoredFoodById(long id);
    StoredFood updateStoredFood(StoredFood storedFood, long id);
    void deleteStoredFood(long id);

    /** Increase/decrease quantity by {@code delta} (negative = decrement). Deletes when quantity reaches 0. */
    StoredFood adjustQuantity(long id, int delta);
}
