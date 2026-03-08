package be.intecbrussel.shoppinglist.service;

import be.intecbrussel.shoppinglist.dto.AddToStorageRequest;
import be.intecbrussel.shoppinglist.dto.AddToStorageResult;
import be.intecbrussel.shoppinglist.model.FoodOriginal;
import be.intecbrussel.shoppinglist.model.Home;
import be.intecbrussel.shoppinglist.model.StorageType;
import be.intecbrussel.shoppinglist.model.StoredFood;
import be.intecbrussel.shoppinglist.repository.FoodOriginalRepository;
import be.intecbrussel.shoppinglist.repository.StorageTypeRepository;
import be.intecbrussel.shoppinglist.repository.StoredFoodRepository;
import be.intecbrussel.shoppinglist.repository.UserHomeRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FoodOriginalServiceImpl implements FoodOriginalService {

    private final FoodOriginalRepository foodOriginalRepository;
    private final StoredFoodRepository   storedFoodRepository;
    private final UserHomeRepository     homeRepository;
    private final StorageTypeRepository  storageTypeRepository;

    /**
     * Create a {@link FoodOriginal} and immediately put it into storage.
     *
     * <h3>Steps</h3>
     * <ol>
     *   <li>Resolve the {@link Home} and {@link StorageType} from the ids in the request.</li>
     *   <li>Build and persist the {@link FoodOriginal}.
     *       <ul>
     *         <li>{@code setOriginal_ml_g()} is called first — this also sets
     *             {@code remaining_ml_g} to the same value (sealed unit shortcut).</li>
     *         <li>{@code setRemaining_ml_g()} is then called with the caller's override
     *             so partially-consumed units are handled correctly.</li>
     *         <li>When the request omits {@code remainingMlG} it defaults to
     *             {@code originalMlG} (nothing opened yet).</li>
     *       </ul>
     *   </li>
     *   <li>Create and persist a {@link StoredFood} (quantity from request, default 1)
     *       that references the new {@link FoodOriginal}.</li>
     * </ol>
     */
    @Override
    @Transactional
    public AddToStorageResult addToStorage(AddToStorageRequest request) {

        // ── 1. Resolve referenced entities ────────────────────────────────────
        Home home = homeRepository.findById(request.getHomeId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Home not found with id: " + request.getHomeId()));

        StorageType storageType = storageTypeRepository.findById(request.getStorageTypeId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "StorageType not found with id: " + request.getStorageTypeId()));

        // ── 2. Build and persist the FoodOriginal ─────────────────────────────
        //
        // remainingMlG defaults to originalMlG when the caller omits it,
        // meaning the unit is still completely sealed.
        double remaining = request.getRemainingMlG() != null
                ? request.getRemainingMlG()
                : request.getOriginalMlG();

        FoodOriginal foodOriginal = new FoodOriginal();
        foodOriginal.setName(request.getName());
        foodOriginal.setRemarks(request.getRemarks());
        foodOriginal.setBestBeforeEnd(request.getBestBeforeEnd());
        foodOriginal.setOriginal_ml_g(request.getOriginalMlG()); // also sets remaining = original
        foodOriginal.setRemaining_ml_g(remaining);               // apply caller's override
        foodOriginal.setUseBy(request.getUseBy());

        FoodOriginal savedFoodOriginal = foodOriginalRepository.save(foodOriginal);
        log.debug("Persisted FoodOriginal id={} name='{}' remaining={}ml/g.",
                savedFoodOriginal.getId(), savedFoodOriginal.getName(), remaining);

        // ── 3. Wrap in a StoredFood and persist ───────────────────────────────
        StoredFood storedFood = new StoredFood(
                0L,
                home,
                savedFoodOriginal,
                storageType,
                request.getQuantity()
        );

        StoredFood savedStoredFood = storedFoodRepository.save(storedFood);
        log.info("Added FoodOriginal id={} to storage: StoredFood id={}, qty={}, home='{}', storageType='{}'.",
                savedFoodOriginal.getId(), savedStoredFood.getId(),
                request.getQuantity(), home.getName(), storageType.getName());

        return AddToStorageResult.builder()
                .foodOriginalId(savedFoodOriginal.getId())
                .storedFoodId(savedStoredFood.getId())
                .quantity(savedStoredFood.getQuantity())
                .remainingMlG(remaining)
                .build();
    }
}
