package be.intecbrussel.shoppinglist.service;

import be.intecbrussel.shoppinglist.dto.FoodOriginalConsumeRequest;
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

    /**
     * Mark a sealed package as opened: set a shorter useBy date and optionally
     * deduct an initial serving.  Throws {@code MissingDataException} when
     * {@code useBy} is after {@code bestBeforeEnd}.
     */
    FoodOriginalResponse openPackage(long id, OpenPackageRequest request);

    /**
     * Record consumption from an already-opened package.
     * Sets {@code remaining_ml_g} to {@code ml_g_left}; auto-soft-deletes when ≤ 0.
     */
    FoodOriginalResponse consume(long id, FoodOriginalConsumeRequest request);
}
