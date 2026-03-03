package be.intecbrussel.shoppinglist.dto;

public record StoredFoodResponse(
        long id,
        HomeResponse home,
        FoodOriginalResponse food,
        StorageTypeResponse storageType,
        int quantity
) {}
