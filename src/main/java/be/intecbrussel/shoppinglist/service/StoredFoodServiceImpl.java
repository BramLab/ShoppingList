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
     * <h3>Why softDeleteById() is used instead of repository.delete()</h3>
     *
     * <p>Calling {@code repository.delete()} puts the entity into Hibernate's REMOVED
     * state. If any still-MANAGED entity (e.g. the new {@code StoredFood}) holds a
     * reference to that REMOVED entity, Hibernate throws a
     * {@code TransientPropertyValueException} or {@code AssertionFailure} at
     * flush/commit time — even with flush() or detach() workarounds.
     *
     * <p>{@code softDeleteById()} fires a direct native {@code UPDATE food SET deleted_at = now()}
     * instead, bypassing the entity lifecycle entirely. The {@code FoodOriginal} stays
     * MANAGED, the FK in {@code StoredFood} stays valid, and no lifecycle error occurs.
     */
    @Override
    @Transactional
    public ConsumeResult consume(long storedFoodId, ConsumeRequest request) {
        log.info("consume() called — storedFoodId={}, qty={}, remainingMlG={}",
                storedFoodId, request.getQuantity(), request.getRemainingMlG());

        // ── 1. Load source ────────────────────────────────────────────────────
        StoredFood source = storedFoodRepository.findById(storedFoodId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "StoredFood not found with id: " + storedFoodId));

        int consumeQty = request.getQuantity();
        int oldQty     = source.getQuantity();
        log.debug("Source StoredFood id={} qty={}, consuming {}.", storedFoodId, oldQty, consumeQty);

        if (consumeQty > oldQty) {
            throw new MissingDataException(
                    "Cannot consume " + consumeQty + " unit(s): only " + oldQty + " in stock.");
        }

        // ── 2. Capture associations before any delete ─────────────────────────
        Food        baseFood          = source.getFood();
        var         sourceHome        = source.getHome();
        StorageType sourceStorageType = source.getStorageType();

        // ── 3. Decrement / delete source pack ─────────────────────────────────
        int  newQty       = oldQty - consumeQty;
        Long sourceIdAfter;

        if (newQty == 0) {
            log.debug("Source StoredFood {} depleted — deleting.", storedFoodId);
            storedFoodRepository.delete(source);
            sourceIdAfter = null;
        } else {
            source.setQuantity(newQty);
            storedFoodRepository.save(source);
            sourceIdAfter = source.getId();
            log.debug("Source StoredFood {} decremented to qty={}.", storedFoodId, newQty);
        }

        // ── 4. Build the opened FoodOriginal ──────────────────────────────────
        FoodOriginal opened    = buildOpenedFoodOriginal(baseFood, request);
        double       remaining = opened.getRemaining_ml_g();
        log.debug("Built opened FoodOriginal: name='{}', remaining_ml_g={}.",
                opened.getName(), remaining);

        // ── 5. Resolve target storage type ────────────────────────────────────
        StorageType targetStorageType = request.getStorageTypeId() != null
                ? storageTypeRepository.findById(request.getStorageTypeId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "StorageType not found: " + request.getStorageTypeId()))
                : sourceStorageType;

        // ── 6. "Mark as empty" path (remainingMlG = 0) ───────────────────────
        if (remaining <= 0) {
            log.debug("remaining <= 0 — taking 'mark as empty' path.");

            // Step A: persist the FoodOriginal.
            FoodOriginal savedFood = foodOriginalRepository.save(opened);
            log.debug("FoodOriginal saved — id={}.", savedFood.getId());

            // Step B: create the StoredFood referencing it.
            StoredFood emptyStoredFood = new StoredFood(
                    0L, sourceHome, savedFood, targetStorageType, consumeQty);
            StoredFood savedEmptyStoredFood = storedFoodRepository.save(emptyStoredFood);
            log.debug("Empty StoredFood id={} saved with FK → FoodOriginal id={}.",
                    savedEmptyStoredFood.getId(), savedFood.getId());

            // Step C: soft-delete via a direct native UPDATE — bypasses Hibernate's
            // entity lifecycle entirely.
            //
            // repository.delete() marks the entity as REMOVED. Any MANAGED entity
            // (emptyStoredFood) that still holds a reference to a REMOVED entity
            // causes TransientPropertyValueException or AssertionFailure at
            // flush/commit time — regardless of flush() or detach() workarounds.
            //
            // softDeleteById() fires "UPDATE food SET deleted_at = now() WHERE id = ?"
            // directly. savedFood stays MANAGED, the FK in StoredFood stays valid,
            // and no lifecycle error occurs.
            foodOriginalRepository.softDeleteById(savedFood.getId());
            log.info("FoodOriginal id={} soft-deleted via native query. Empty StoredFood id={} retained for inventory.",
                    savedFood.getId(), savedEmptyStoredFood.getId());

            return ConsumeResult.builder()
                    .sourceStoredFoodId(sourceIdAfter)
                    .sourceRemainingQuantity(newQty)
                    .openedStoredFoodId(savedEmptyStoredFood.getId())
                    .consumedQuantity(consumeQty)
                    .openedRemainingMlG(0)
                    .openedUnitWasEmpty(true)
                    .build();
        }

        // ── 7. Normal path (unit has remaining content) ───────────────────────
        log.debug("remaining={} > 0 — taking normal open path.", remaining);
        FoodOriginal savedOpened = foodOriginalRepository.save(opened);

        StoredFood openedStoredFood = new StoredFood(
                0L, sourceHome, savedOpened, targetStorageType, consumeQty);
        StoredFood savedOpenedStoredFood = storedFoodRepository.save(openedStoredFood);

        log.info("Consumed {} unit(s) from StoredFood {}. New opened StoredFood id={} (qty={}). Remaining in pack: {}.",
                consumeQty, storedFoodId, savedOpenedStoredFood.getId(), consumeQty, newQty);

        return ConsumeResult.builder()
                .sourceStoredFoodId(sourceIdAfter)
                .sourceRemainingQuantity(newQty)
                .openedStoredFoodId(savedOpenedStoredFood.getId())
                .consumedQuantity(consumeQty)
                .openedRemainingMlG(remaining)
                .openedUnitWasEmpty(false)
                .build();
    }

    // ── Private helpers ───────────────────────────────────────────────────────

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

    private StoredFood findEntity(long id) {
        return storedFoodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "StoredFood not found with id: " + id));
    }
}