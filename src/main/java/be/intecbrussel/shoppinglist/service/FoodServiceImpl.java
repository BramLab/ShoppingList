package be.intecbrussel.shoppinglist.service;

import be.intecbrussel.shoppinglist.exception.MissingDataException;
import be.intecbrussel.shoppinglist.exception.ResourceNotFoundException;
import be.intecbrussel.shoppinglist.model.Food;
import be.intecbrussel.shoppinglist.model.FoodOriginal;
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

    // ── Generic Food ────────────────────────────────────────────────────────────

    @Override
    public List<Food> findAllFoods() {
        return foodRepository.findAll();
    }

    @Override
    public Food findFoodById(long id) {
        return foodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Food not found with id: " + id));
    }

    // ── FoodOriginal CRUD ───────────────────────────────────────────────────────

    @Override
    public FoodOriginal saveFood(FoodOriginal food) {
        if (food.getName() == null || food.getName().isBlank()) {
            throw new MissingDataException("Food name is required");
        }
        if (food.getOriginal_ml_g() <= 0) {
            throw new MissingDataException("original_ml_g must be greater than 0");
        }
        if (food.getBestBeforeEnd() == null) {
            throw new MissingDataException("bestBeforeEnd date is required");
        }
        return foodOriginalRepository.save(food);
    }

    @Override
    public FoodOriginal updateFood(FoodOriginal incoming, long id) {
        FoodOriginal existing = findFoodOriginalById(id);

        if (incoming.getName() != null && !incoming.getName().isBlank()) {
            existing.setName(incoming.getName());
        }
        if (incoming.getRemarks() != null) {
            existing.setRemarks(incoming.getRemarks());
        }
        if (incoming.getBestBeforeEnd() != null) {
            existing.setBestBeforeEnd(incoming.getBestBeforeEnd());
        }
        if (incoming.getUseBy() != null) {
            existing.setUseBy(incoming.getUseBy());
        }
        if (incoming.getOriginal_ml_g() > 0) {
            // setOriginal_ml_g also resets remaining — only apply when explicitly updating pack size
            existing.setOriginal_ml_g(incoming.getOriginal_ml_g());
        }
        if (incoming.getRemaining_ml_g() >= 0) {
            existing.setRemaining_ml_g(incoming.getRemaining_ml_g());
        }
        return foodOriginalRepository.save(existing);
    }

    @Override
    public void deleteFood(long id) {
        findFoodOriginalById(id); // verify existence before deleting
        foodOriginalRepository.deleteById(id); // triggers @SoftDelete
    }

    // ── Domain actions ──────────────────────────────────────────────────────────

    @Override
    public FoodOriginal consume(long id, double amount) {
        if (amount <= 0) {
            throw new MissingDataException("Consumption amount must be greater than 0");
        }
        FoodOriginal food = findFoodOriginalById(id);

        double newRemaining = food.getRemaining_ml_g() - amount;
        food.setRemaining_ml_g(newRemaining); // setter soft-deletes when ≤ 0
        return foodOriginalRepository.save(food);
    }

    @Override
    public FoodOriginal openPackage(long id, LocalDate useBy, double initialConsumption) {
        FoodOriginal food = findFoodOriginalById(id);

        if (useBy == null) {
            throw new MissingDataException("useBy date is required when opening a package");
        }
        if (useBy.isAfter(food.getBestBeforeEnd())) {
            throw new MissingDataException("useBy date cannot be after bestBeforeEnd");
        }
        food.setUseBy(useBy);

        if (initialConsumption > 0) {
            double newRemaining = food.getRemaining_ml_g() - initialConsumption;
            food.setRemaining_ml_g(newRemaining);
        }
        return foodOriginalRepository.save(food);
    }

    // ── Private helpers ─────────────────────────────────────────────────────────

    private FoodOriginal findFoodOriginalById(long id) {
        return foodOriginalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FoodOriginal not found with id: " + id));
    }
}
