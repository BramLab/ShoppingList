package be.intecbrussel.shoppinglist.service;

import be.intecbrussel.shoppinglist.dto.FoodOriginalConsumeRequest;
import be.intecbrussel.shoppinglist.dto.FoodOriginalMapper;
import be.intecbrussel.shoppinglist.dto.FoodOriginalRequest;
import be.intecbrussel.shoppinglist.dto.FoodOriginalResponse;
import be.intecbrussel.shoppinglist.dto.FoodOriginalUpdateRequest;
import be.intecbrussel.shoppinglist.dto.OpenPackageRequest;
import be.intecbrussel.shoppinglist.exception.MissingDataException;
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

    /**
     * Mark a sealed package as opened.
     *
     * <ul>
     *   <li>Sets {@code useBy} (must not exceed {@code bestBeforeEnd}).</li>
     *   <li>Deducts {@code initialConsumption} from {@code remaining_ml_g}.
     *       The package is auto-soft-deleted when remaining drops to ≤ 0.</li>
     * </ul>
     */
    @Override
    public FoodOriginalResponse openPackage(long id, OpenPackageRequest request) {
        FoodOriginal food = findEntity(id);

        if (food.getBestBeforeEnd() != null
                && request.useBy().isAfter(food.getBestBeforeEnd())) {
            throw new MissingDataException(
                    "useBy date (" + request.useBy()
                    + ") cannot be after bestBeforeEnd (" + food.getBestBeforeEnd() + ")");
        }

        food.setUseBy(request.useBy());

        if (request.initialConsumption() > 0) {
            // setRemaining_ml_g() handles the ≤ 0 → -1 sentinel automatically
            food.setRemaining_ml_g(food.getRemaining_ml_g() - request.initialConsumption());
        }

        return FoodOriginalMapper.mapToFoodOriginalResponse(foodOriginalRepository.save(food));
    }

    /**
     * Record how much product remains after a serving.
     * {@code ml_g_left} becomes the new {@code remaining_ml_g}.
     * The setter converts ≤ 0 to -1 (empty sentinel) automatically.
     */
    @Override
    public FoodOriginalResponse consume(long id, FoodOriginalConsumeRequest request) {
        FoodOriginal food = findEntity(id);
        food.setUseBy(request.useBy());
        food.setRemaining_ml_g(request.ml_g_left()); // setter soft-deletes at ≤ 0
        return FoodOriginalMapper.mapToFoodOriginalResponse(foodOriginalRepository.save(food));
    }

    // Package-private: lets StoredFoodServiceImpl resolve a FoodOriginal entity.
    FoodOriginal findEntity(long id) {
        return foodOriginalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Food not found with id: " + id));
    }
}
