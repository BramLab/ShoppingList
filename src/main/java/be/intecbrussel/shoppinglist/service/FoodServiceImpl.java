package be.intecbrussel.shoppinglist.service;

import be.intecbrussel.shoppinglist.dto.FoodOriginalConsumeRequest;
import be.intecbrussel.shoppinglist.dto.FoodOriginalMapper;
import be.intecbrussel.shoppinglist.dto.FoodOriginalRequest;
import be.intecbrussel.shoppinglist.dto.FoodOriginalResponse;
import be.intecbrussel.shoppinglist.dto.FoodOriginalUpdateRequest;
import be.intecbrussel.shoppinglist.exception.ResourceNotFoundException;
import be.intecbrussel.shoppinglist.model.FoodOriginal;
import be.intecbrussel.shoppinglist.repository.FoodOriginalRepository;
import be.intecbrussel.shoppinglist.repository.FoodRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class FoodServiceImpl implements FoodService {

    private final FoodRepository foodRepository;
    private final FoodOriginalRepository foodOriginalRepository;

    @Override
    public FoodOriginalResponse saveFood(FoodOriginalRequest request) {
        FoodOriginal saved = foodOriginalRepository.save(
                FoodOriginalMapper.mapToFoodOriginal(request));
        return FoodOriginalMapper.mapToFoodOriginalResponse(saved);
    }

    @Override
    public List<FoodOriginalResponse> findAllFoods() {
        // FoodRepository covers the full hierarchy; cast to FoodOriginal where applicable.
        return foodRepository.findAll()
                .stream()
                .filter(f -> f instanceof FoodOriginal)
                .map(f -> FoodOriginalMapper.mapToFoodOriginalResponse((FoodOriginal) f))
                .toList();
    }

    @Override
    public FoodOriginalResponse findFoodById(long id) {
        return FoodOriginalMapper.mapToFoodOriginalResponse(findEntity(id));
    }

    @Override
    public FoodOriginalResponse updateFood(long id, FoodOriginalUpdateRequest request) {
        FoodOriginal existing = findEntity(id);

        if (request.name() != null && !request.name().isBlank()) {
            existing.setName(request.name());
        }
        if (request.remarks() != null) {
            existing.setRemarks(request.remarks());
        }
        if (request.bestBeforeEnd() != null) {
            existing.setBestBeforeEnd(request.bestBeforeEnd());
        }
        if (request.useBy() != null) {
            existing.setUseBy(request.useBy());
        }
        if (request.original_ml_g() != null && request.original_ml_g() > 0) {
            existing.setOriginal_ml_g(request.original_ml_g()); // also resets remaining
        }
        if (request.remaining_ml_g() != null) {
            existing.setRemaining_ml_g(request.remaining_ml_g());
        }
        return FoodOriginalMapper.mapToFoodOriginalResponse(foodOriginalRepository.save(existing));
    }

    @Override
    public void deleteFood(long id) {
        findEntity(id); // verify existence
        foodOriginalRepository.deleteById(id); // triggers @SoftDelete
    }

    /** Consumption: If multiple products, take 1 away from multiple (presumably oldest first) and add separately again
     * with shorter useBy, reduced remaining_ml_g.
     * Auto-soft-delete when empty. */
    @Override
    public FoodOriginalResponse consume(long id, FoodOriginalConsumeRequest request) {
        FoodOriginal food = findEntity(id);
        double newRemaining = request.ml_g_left();
        food.setRemaining_ml_g(newRemaining); // setter soft-deletes at ≤ 0
        return FoodOriginalMapper.mapToFoodOriginalResponse(foodOriginalRepository.save(food));
    }

//    @Override
//    public FoodOriginalResponse openPackage(long id, OpenPackageRequest request) {
//        FoodOriginal food = findEntity(id);
//
//        if (request.useBy().isAfter(food.getBestBeforeEnd())) {
//            throw new MissingDataException("useBy date cannot be after bestBeforeEnd");
//        }
//        food.setUseBy(request.useBy());
//
//        if (request.initialConsumption() > 0) {
//            food.setRemaining_ml_g(food.getRemaining_ml_g() - request.initialConsumption());
//        }
//        return FoodOriginalMapper.mapToFoodOriginalResponse(foodOriginalRepository.save(food));
//    }

    // Package-private: lets StoredFoodServiceImpl resolve a FoodOriginal entity.
    FoodOriginal findEntity(long id) {
        return foodOriginalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Food not found with id: " + id));
    }
}
