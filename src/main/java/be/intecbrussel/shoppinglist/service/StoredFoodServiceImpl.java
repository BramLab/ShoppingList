package be.intecbrussel.shoppinglist.service;

import be.intecbrussel.shoppinglist.dto.*;
import be.intecbrussel.shoppinglist.exception.MissingDataException;
import be.intecbrussel.shoppinglist.exception.ResourceNotFoundException;
import be.intecbrussel.shoppinglist.model.Food;
import be.intecbrussel.shoppinglist.model.FoodOriginal;
import be.intecbrussel.shoppinglist.model.StorageType;
import be.intecbrussel.shoppinglist.model.StoredFood;
import be.intecbrussel.shoppinglist.repository.FoodOriginalRepository;
import be.intecbrussel.shoppinglist.repository.StorageTypeRepository;
import be.intecbrussel.shoppinglist.repository.StoredFoodRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class StoredFoodServiceImpl implements StoredFoodService {

    private final StoredFoodRepository   storedFoodRepository;
    private final StoredFoodMapper       storedFoodMapper;
    private final FoodOriginalRepository foodOriginalRepository;
    private final StorageTypeRepository  storageTypeRepository;

    @Override
    public StoredFoodResponse saveStoredFood(StoredFoodRequest request) {
        StoredFood saved = storedFoodRepository.save(
                storedFoodMapper.mapToStoredFood(request));
        return StoredFoodMapper.mapToStoredFoodResponse(saved);
    }

    @Override
    public List<StoredFoodResponse> findAllStoredFoods() {
        return storedFoodRepository.findAllWithFood()
                .stream()
                .map(StoredFoodMapper::mapToStoredFoodResponse)
                .toList();
    }

    @Override
    public List<StoredFoodResponse> findAllByHomeId(long homeId) {
        return storedFoodRepository.findAllByHomeId(homeId)
                .stream()
                .map(StoredFoodMapper::mapToStoredFoodResponse)
                .toList();
    }

    @Override
    public StoredFoodResponse findStoredFoodById(long id) {
        return StoredFoodMapper.mapToStoredFoodResponse(findEntity(id));
    }

    @Override
    public StoredFoodResponse updateStoredFood(long id, StoredFoodUpdateRequest request) {
        StoredFood existing = findEntity(id);

        if (request.storageTypeId() != null) {
            StorageType storageType = storageTypeRepository.findById(request.storageTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "StorageType not found with id: " + request.storageTypeId()));
            existing.setStorageType(storageType);
        }
        if (request.quantity() != null) {
            if (request.quantity() < 0) {
                throw new MissingDataException("Quantity cannot be negative");
            }
            existing.setQuantity(request.quantity());
        }
        return StoredFoodMapper.mapToStoredFoodResponse(storedFoodRepository.save(existing));
    }

    @Override
    public void deleteStoredFood(long id) {
        findEntity(id);
        storedFoodRepository.deleteById(id);
    }

    @Override
    public StoredFoodResponse adjustQuantity(long id, AdjustQuantityRequest request) {
        StoredFood existing = findEntity(id);
        int newQty = existing.getQuantity() + request.delta();

        if (newQty < 0) {
            throw new MissingDataException(
                    "Quantity cannot drop below 0 (current: " + existing.getQuantity()
                            + ", delta: " + request.delta() + ")");
        }
        if (newQty == 0) {
            storedFoodRepository.deleteById(id);
            return StoredFoodMapper.mapToStoredFoodResponse(existing);
        }
        existing.setQuantity(newQty);
        return StoredFoodMapper.mapToStoredFoodResponse(storedFoodRepository.save(existing));
    }

    /**
     * Take {@code request.quantity} sealed units out of a stored pack.
     *
     * <p>Steps performed:
     * <ol>
     *   <li>Validate that the requested quantity does not exceed what is in stock.</li>
     *   <li>Decrement {@code StoredFood.quantity} by {@code request.quantity}.
     *       When quantity reaches 0 the source {@code StoredFood} row is deleted.</li>
     *   <li>Create a brand-new {@code FoodOriginal} copying name/remarks from the base food,
     *       with the provided {@code remainingMlG}, {@code useBy}, and {@code bestBeforeEnd}.</li>
     *   <li>Wrap that {@code FoodOriginal} in a new {@code StoredFood} with
     *       {@code quantity = request.quantity}, so the N opened units are tracked together.</li>
     *   <li>If {@code remainingMlG ≤ 0} the opened unit is immediately soft-deleted
     *       and no {@code StoredFood} row is created for it.</li>
     * </ol>
     */
    @Override
    @Transactional
    public ConsumeResult consume(long storedFoodId, ConsumeRequest request) {

        StoredFood source = storedFoodRepository.findById(storedFoodId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "StoredFood not found with id: " + storedFoodId));

        int consumeQty = request.getQuantity();
        int oldQty     = source.getQuantity();

        // Guard: cannot consume more units than are in stock
        if (consumeQty > oldQty) {
            throw new MissingDataException(
                    "Cannot consume " + consumeQty + " unit(s): only " + oldQty + " in stock.");
        }

        // Eagerly capture references before any delete — lazy fields become inaccessible
        // once the entity is removed from the persistence context.
        Food        baseFood    = source.getFood();
        var         sourceHome  = source.getHome();
        StorageType sourceStorageType = source.getStorageType();

        int  newQty   = oldQty - consumeQty;
        Long sourceIdAfter;

        if (newQty == 0) {
            log.debug("StoredFood {} is now empty after consuming {}; deleting it.", storedFoodId, consumeQty);
            storedFoodRepository.delete(source);
            sourceIdAfter = null;
        } else {
            source.setQuantity(newQty);
            storedFoodRepository.save(source);
            sourceIdAfter = source.getId();
            log.debug("StoredFood {} decremented by {} to qty={}.", storedFoodId, consumeQty, newQty);
        }

        FoodOriginal opened    = buildOpenedFoodOriginal(baseFood, request);
        double       remaining = opened.getRemaining_ml_g();

        if (remaining <= 0) {
            // Soft-delete the FoodOriginal so it appears on the Deleted Foods page.
            FoodOriginal saved = foodOriginalRepository.save(opened);
            foodOriginalRepository.delete(saved);

            // Still create a StoredFood row (quantity = consumeQty) so the main list
            // shows N empty units rather than silently discarding them.
            StorageType emptyStorageType = request.getStorageTypeId() != null
                    ? storageTypeRepository.findById(request.getStorageTypeId())
                    .orElseThrow(() -> new EntityNotFoundException("StorageType not found: " + request.getStorageTypeId()))
                    : sourceStorageType;
            StoredFood emptyStoredFood = new StoredFood(
                    0L,
                    sourceHome,
                    saved,
                    emptyStorageType,
                    consumeQty
            );
            StoredFood savedEmptyStoredFood = storedFoodRepository.save(emptyStoredFood);
            log.debug("Consumed {} unit(s) of '{}' marked empty; StoredFood id={} qty={}.",
                    consumeQty, baseFood.getName(), savedEmptyStoredFood.getId(), consumeQty);

            return ConsumeResult.builder()
                    .sourceStoredFoodId(sourceIdAfter)
                    .sourceRemainingQuantity(newQty)
                    .openedStoredFoodId(savedEmptyStoredFood.getId())
                    .consumedQuantity(consumeQty)
                    .openedRemainingMlG(0)
                    .openedUnitWasEmpty(true)
                    .build();
        }

        FoodOriginal savedOpened     = foodOriginalRepository.save(opened);
        StorageType  targetStorageType = request.getStorageTypeId() != null
                ? storageTypeRepository.findById(request.getStorageTypeId())
                .orElseThrow(() -> new EntityNotFoundException("StorageType not found: " + request.getStorageTypeId()))
                : sourceStorageType;

        // The opened tracking entry always has quantity = 1.
        // Consuming N sealed units removes N from the source; the single opened
        // entry represents the now-open container being used — it does not
        // add N back to the visible stock total.
        StoredFood openedStoredFood = new StoredFood(
                0L,
                sourceHome,
                savedOpened,
                targetStorageType,
                1
        );
        StoredFood savedOpenedStoredFood = storedFoodRepository.save(openedStoredFood);

        log.info("Consumed {} unit(s) from StoredFood {}. New opened StoredFood id={} (qty=1). Remaining in pack: {}.",
                consumeQty, storedFoodId, savedOpenedStoredFood.getId(), newQty);

        return ConsumeResult.builder()
                .sourceStoredFoodId(sourceIdAfter)
                .sourceRemainingQuantity(newQty)
                .openedStoredFoodId(savedOpenedStoredFood.getId())
                .consumedQuantity(consumeQty)
                .openedRemainingMlG(remaining)
                .openedUnitWasEmpty(false)
                .build();
    }

    // ── Private helpers ───────────────────────────────────────────────────────────

    private FoodOriginal buildOpenedFoodOriginal(Food baseFood, ConsumeRequest request) {
        double    originalMlG = 0;
        LocalDate bestBefore  = null;

        if (baseFood instanceof FoodOriginal fo) {
            originalMlG = fo.getOriginal_ml_g();
            bestBefore  = fo.getBestBeforeEnd();
        }

        FoodOriginal opened = new FoodOriginal();
        opened.setName(baseFood.getName());
        opened.setRemarks(baseFood.getRemarks());
        opened.setBestBeforeEnd(bestBefore);
        opened.setOriginal_ml_g(originalMlG);
        opened.setRemaining_ml_g(request.getRemainingMlG());
        opened.setUseBy(request.getUseBy());

        return opened;
    }

    private StorageType resolveStorageType(ConsumeRequest request, StoredFood source) {
        if (request.getStorageTypeId() == null) {
            return source.getStorageType();
        }
        return storageTypeRepository.findById(request.getStorageTypeId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "StorageType not found with id: " + request.getStorageTypeId()));
    }

    private StoredFood findEntity(long id) {
        return storedFoodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StoredFood not found with id: " + id));
    }
}