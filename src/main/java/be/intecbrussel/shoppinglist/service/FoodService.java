package be.intecbrussel.shoppinglist.service;

import be.intecbrussel.shoppinglist.dto.ConsumeRequest;
import be.intecbrussel.shoppinglist.dto.FoodOriginalRequest;
import be.intecbrussel.shoppinglist.dto.FoodOriginalResponse;
import be.intecbrussel.shoppinglist.dto.FoodOriginalUpdateRequest;
import be.intecbrussel.shoppinglist.dto.OpenPackageRequest;

import java.util.List;

public interface FoodService {
    FoodOriginalResponse saveFood(FoodOriginalRequest request);
    List<FoodOriginalResponse> findAllFoods();
    FoodOriginalResponse findFoodById(long id);
    FoodOriginalResponse updateFood(long id, FoodOriginalUpdateRequest request);
    void deleteFood(long id);

    /** Record consumption: reduce remaining_ml_g. Auto-soft-deletes when empty. */
    FoodOriginalResponse consume(long id, ConsumeRequest request);

    /** Open a sealed package: set a shorter useBy and optionally take an initial serving. */
    FoodOriginalResponse openPackage(long id, OpenPackageRequest request);
}
