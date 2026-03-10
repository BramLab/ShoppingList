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
        // findAllWithFood uses LEFT JOIN FETCH to bypass Hibernate's per-row
        // soft-delete filter that would otherwise exclude restored-food entries.
        return storedFoodRepository.findAllWithFood()
                .stream()
                .map(StoredFoodMapper::mapToStoredFoodResponse)
                .toList();
    }

    /**
     * Uses the repository-level LEFT JOIN FETCH query instead of the inherited
     * findAll, for the same reason as findAllStoredFoods.
     */
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

    @Override
    @Transactional
    public ConsumeResult consume(long storedFoodId, ConsumeRequest request) {

        StoredFood source = storedFoodRepository.findById(storedFoodId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "StoredFood not found with id: " + storedFoodId));

        Food baseFood = source.getFood();
        int  oldQty   = source.getQuantity();

        int  newQty      = oldQty - 1;
        Long sourceIdAfter;

        if (newQty <= 0) {
            log.debug("StoredFood {} was the last unit; deleting it.", storedFoodId);
            storedFoodRepository.delete(source);
            sourceIdAfter = null;
        } else {
            source.setQuantity(newQty);
            storedFoodRepository.save(source);
            sourceIdAfter = source.getId();
            log.debug("StoredFood {} decremented to qty={}.", storedFoodId, newQty);
        }

        FoodOriginal opened = buildOpenedFoodOriginal(baseFood, request);

        double remaining = opened.getRemaining_ml_g();

        if (remaining <= 0) {
            FoodOriginal saved = foodOriginalRepository.save(opened);
            foodOriginalRepository.delete(saved);
            log.debug("Opened unit of '{}' was immediately empty; soft-deleted FoodOriginal {}.",
                    baseFood.getName(), saved.getId());

            return ConsumeResult.builder()
                    .sourceStoredFoodId(sourceIdAfter)
                    .sourceRemainingQuantity(newQty)
                    .openedStoredFoodId(null)
                    .openedRemainingMlG(0)
                    .openedUnitWasEmpty(true)
                    .build();
        }

        FoodOriginal savedOpened = foodOriginalRepository.save(opened);

        StorageType targetStorageType = resolveStorageType(request, source);

        StoredFood openedStoredFood = new StoredFood(
                0L,
                source.getHome(),
                savedOpened,
                targetStorageType,
                1
        );
        StoredFood savedOpenedStoredFood = storedFoodRepository.save(openedStoredFood);

        log.info("Consumed 1 unit from StoredFood {}. New opened StoredFood id={}. Remaining in pack: {}.",
                storedFoodId, savedOpenedStoredFood.getId(), newQty);

        return ConsumeResult.builder()
                .sourceStoredFoodId(sourceIdAfter)
                .sourceRemainingQuantity(newQty)
                .openedStoredFoodId(savedOpenedStoredFood.getId())
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