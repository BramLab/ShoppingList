package be.intecbrussel.shoppinglist.service;

import be.intecbrussel.shoppinglist.dto.DeletedFoodResponse;
import be.intecbrussel.shoppinglist.dto.FoodOriginalConsumeRequest;
import be.intecbrussel.shoppinglist.dto.FoodOriginalMapper;
import be.intecbrussel.shoppinglist.dto.FoodOriginalRequest;
import be.intecbrussel.shoppinglist.dto.FoodOriginalResponse;
import be.intecbrussel.shoppinglist.dto.FoodOriginalUpdateRequest;
import be.intecbrussel.shoppinglist.dto.OpenPackageRequest;
import be.intecbrussel.shoppinglist.exception.MissingDataException;
import be.intecbrussel.shoppinglist.exception.ResourceNotFoundException;
import be.intecbrussel.shoppinglist.model.FoodOriginal;
import be.intecbrussel.shoppinglist.repository.FoodDeletedView;
import be.intecbrussel.shoppinglist.repository.FoodOriginalRepository;
import be.intecbrussel.shoppinglist.repository.FoodRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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

    /**
     * Returns every soft-deleted FoodOriginal, mapped to the standard response DTO.
     *
     * Uses a native query via the repository because Hibernate's @SoftDelete filter
     * silently excludes deleted rows from all standard JPQL / entity queries.
     * The projection FoodDeletedView is manually converted here rather than extending
     * FoodOriginalMapper, so no existing code is touched.
     */
    @Override
    public List<DeletedFoodResponse> findAllDeletedFoods() {
        return foodOriginalRepository.findAllDeleted()
                .stream()
                .map(FoodServiceImpl::mapDeletedViewToResponse)
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
     * Restores a soft-deleted food by clearing its deleted_at timestamp.
     *
     * Steps:
     * 1. Verify the id exists at all (including deleted rows) — 404 if not.
     * 2. Clear deleted_at via a native UPDATE (bypasses the @SoftDelete filter).
     * 3. Re-load the now-active entity and return its DTO.
     */
    @Override
    public FoodOriginalResponse restoreFood(long id) {
        if (foodOriginalRepository.countIncludingDeleted(id) == 0) {
            throw new ResourceNotFoundException("Food not found with id: " + id);
        }
        foodOriginalRepository.restoreById(id);

        // After the UPDATE the entity is active and findById works normally again.
        FoodOriginal restored = foodOriginalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Food could not be restored for id: " + id));
        return FoodOriginalMapper.mapToFoodOriginalResponse(restored);
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

    // ── Private helpers ───────────────────────────────────────────────────────────

    // Package-private: lets StoredFoodServiceImpl resolve a FoodOriginal entity.
    FoodOriginal findEntity(long id) {
        return foodOriginalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Food not found with id: " + id));
    }

    /**
     * Converts a native-query projection to the standard response DTO.
     * Mirrors FoodOriginalMapper#mapToFoodOriginalResponse but works from
     * the FoodDeletedView projection instead of a managed entity.
     */
    private static DeletedFoodResponse mapDeletedViewToResponse(FoodDeletedView v) {
        LocalDate effectiveUseBy = v.getUseBy() != null ? v.getUseBy() : v.getBestBeforeEnd();
        boolean empty = v.getRemaining_ml_g() == -1d;
        return new DeletedFoodResponse(
                v.getId(),
                v.getName(),
                v.getRemarks(),
                v.getBestBeforeEnd(),
                v.getOriginal_ml_g(),
                v.getUseBy(),
                v.getRemaining_ml_g(),
                effectiveUseBy,
                empty,
                v.getUpdatedAt()
        );
    }
}