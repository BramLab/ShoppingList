package be.intecbrussel.shoppinglist.dto;

import be.intecbrussel.shoppinglist.exception.ResourceNotFoundException;
import be.intecbrussel.shoppinglist.model.Food;
import be.intecbrussel.shoppinglist.model.FoodOriginal;
import be.intecbrussel.shoppinglist.model.Home;
import be.intecbrussel.shoppinglist.model.StorageType;
import be.intecbrussel.shoppinglist.model.StoredFood;
import be.intecbrussel.shoppinglist.repository.FoodOriginalRepository;
import be.intecbrussel.shoppinglist.repository.StorageTypeRepository;
import be.intecbrussel.shoppinglist.repository.UserHomeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Unlike the other stateless mappers, StoredFoodMapper is a Spring component
 * because resolving a StoredFoodRequest requires database lookups for
 * Home, Food, and StorageType.
 */
@Component
@RequiredArgsConstructor
public class StoredFoodMapper {

    private final UserHomeRepository homeRepository;
    private final FoodOriginalRepository foodOriginalRepository;
    private final StorageTypeRepository storageTypeRepository;

    public StoredFood mapToStoredFood(StoredFoodRequest request) {
        Home home = homeRepository.findById(request.homeId())
                .orElseThrow(() -> new ResourceNotFoundException("Home not found with id: " + request.homeId()));

        FoodOriginal food = foodOriginalRepository.findById(request.foodId())
                .orElseThrow(() -> new ResourceNotFoundException("Food not found with id: " + request.foodId()));

        StorageType storageType = storageTypeRepository.findById(request.storageTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("StorageType not found with id: " + request.storageTypeId()));

        return new StoredFood(0, home, food, storageType, request.quantity());
    }

    public static StoredFoodResponse mapToStoredFoodResponse(StoredFood storedFood) {
        Food food = storedFood.getFood();

        /*
         * food can be null when its FoodOriginal has been soft-deleted:
         * Hibernate's @SoftDelete filter on Food means the LEFT JOIN returns
         * null for the food columns instead of excluding the row.
         * The frontend shows these as "food deleted" placeholders until the food
         * is restored, at which point this mapping produces a full response.
         */
        FoodOriginalResponse foodResponse = (food instanceof FoodOriginal fo)
                ? FoodOriginalMapper.mapToFoodOriginalResponse(fo)
                : null;

        return new StoredFoodResponse(
                storedFood.getId(),
                HomeMapper.mapToHomeResponse(storedFood.getHome()),
                foodResponse,
                StorageTypeMapper.mapToStorageTypeResponse(storedFood.getStorageType()),
                storedFood.getQuantity()
        );
    }
}