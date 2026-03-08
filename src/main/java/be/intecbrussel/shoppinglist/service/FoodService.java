package be.intecbrussel.shoppinglist.service;

import be.intecbrussel.shoppinglist.dto.FoodOriginalConsumeRequest;
import be.intecbrussel.shoppinglist.dto.FoodOriginalRequest;
import be.intecbrussel.shoppinglist.dto.FoodOriginalResponse;
import be.intecbrussel.shoppinglist.dto.FoodOriginalUpdateRequest;

import java.util.List;

public interface FoodService {
    FoodOriginalResponse saveFood(FoodOriginalRequest request);
    List<FoodOriginalResponse> findAllFoods();
    FoodOriginalResponse findFoodById(long id);
    FoodOriginalResponse updateFood(long id, FoodOriginalUpdateRequest request);
    void deleteFood(long id);

    /** Consumption: If multiple products, take 1 away from multiple and add separately again
     * with shorter useBy, reduced remaining_ml_g.
     * Auto-soft-delete when empty. */
    FoodOriginalResponse consume(long id, FoodOriginalConsumeRequest request);

    /** Open a sealed package: set a shorter useBy and optionally take an initial serving. */
    //FoodOriginalResponse openPackage(long id, OpenPackageRequest request);
}
