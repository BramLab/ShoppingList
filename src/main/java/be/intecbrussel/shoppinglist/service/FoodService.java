package be.intecbrussel.shoppinglist.service;

import be.intecbrussel.shoppinglist.model.Food;
import be.intecbrussel.shoppinglist.model.FoodOriginal;

import java.util.List;

public interface FoodService {

    // ── Generic Food (read-only across the hierarchy) ──────────────────────────
    List<Food> findAllFoods();
    Food findFoodById(long id);

    // ── FoodOriginal CRUD ───────────────────────────────────────────────────────
    FoodOriginal saveFood(FoodOriginal food);
    FoodOriginal updateFood(FoodOriginal food, long id);

    /** Soft-delete via Hibernate @SoftDelete */
    void deleteFood(long id);

    // ── Domain actions ──────────────────────────────────────────────────────────
    /**
     * Record consumption: reduce remaining_ml_g by {@code amount}.
     * Soft-deletes the item automatically when nothing is left.
     */
    FoodOriginal consume(long id, double amount);

    /**
     * Open a sealed package: set a shorter useBy date and optionally
     * reduce remaining_ml_g to reflect an initial serving already taken.
     */
    FoodOriginal openPackage(long id, java.time.LocalDate useBy, double initialConsumption);
}
