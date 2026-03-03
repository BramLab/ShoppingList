package be.intecbrussel.shoppinglist.dto;

import be.intecbrussel.shoppinglist.model.FoodOriginal;

public class FoodOriginalMapper {

    public static FoodOriginalResponse mapToFoodOriginalResponse(FoodOriginal food) {
        return new FoodOriginalResponse(
                food.getId(),
                food.getName(),
                food.getRemarks(),
                food.getBestBeforeEnd(),
                food.getOriginal_ml_g(),
                food.getUseBy(),
                food.getRemaining_ml_g(),
                food.getUseByElseBestBeforeEnd(),
                food.getRemaining_ml_g() == -1d
        );
    }

    /**
     * Maps a create request to a new FoodOriginal.
     *
     * IMPORTANT: Lombok's @Builder calls the all-args constructor directly and bypasses
     * the custom setOriginal_ml_g() setter (which would also initialise remaining_ml_g).
     * remaining_ml_g must therefore be set explicitly here.
     */
    public static FoodOriginal mapToFoodOriginal(FoodOriginalRequest request) {
        return FoodOriginal.foodOriginalBuilder()
                .name(request.name())
                .remarks(request.remarks())
                .bestBeforeEnd(request.bestBeforeEnd())
                .original_ml_g(request.original_ml_g())
                .remaining_ml_g(request.original_ml_g()) // explicit — builder bypasses setter
                .build();
    }

    /**
     * Maps an update request to a FoodOriginal used by the service's patch logic.
     * Null fields mean "leave unchanged" — the service checks each field individually.
     */
    public static FoodOriginal mapToFoodOriginal(FoodOriginalUpdateRequest request) {
        FoodOriginal food = new FoodOriginal();
        food.setName(request.name());
        food.setRemarks(request.remarks());
        food.setBestBeforeEnd(request.bestBeforeEnd());
        food.setUseBy(request.useBy());
        if (request.original_ml_g() != null) {
            food.setOriginal_ml_g(request.original_ml_g()); // setter also resets remaining_ml_g
        }
        if (request.remaining_ml_g() != null) {
            food.setRemaining_ml_g(request.remaining_ml_g());
        }
        return food;
    }
}