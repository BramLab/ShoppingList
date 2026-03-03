package be.intecbrussel.shoppinglist.service;

import be.intecbrussel.shoppinglist.dto.AdjustQuantityRequest;
import be.intecbrussel.shoppinglist.dto.StoredFoodMapper;
import be.intecbrussel.shoppinglist.dto.StoredFoodRequest;
import be.intecbrussel.shoppinglist.dto.StoredFoodResponse;
import be.intecbrussel.shoppinglist.dto.StoredFoodUpdateRequest;
import be.intecbrussel.shoppinglist.exception.MissingDataException;
import be.intecbrussel.shoppinglist.exception.ResourceNotFoundException;
import be.intecbrussel.shoppinglist.model.StorageType;
import be.intecbrussel.shoppinglist.model.StoredFood;
import be.intecbrussel.shoppinglist.repository.StoredFoodRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class StoredFoodServiceImpl implements StoredFoodService {

    private final StoredFoodRepository storedFoodRepository;
    private final StoredFoodMapper storedFoodMapper;       // @Component — resolves IDs → entities on create
    private final StorageTypeServiceImpl storageTypeService; // concrete Impl to reach findEntity()

    @Override
    public StoredFoodResponse saveStoredFood(StoredFoodRequest request) {
        StoredFood saved = storedFoodRepository.save(
                storedFoodMapper.mapToStoredFood(request));
        return StoredFoodMapper.mapToStoredFoodResponse(saved);
    }

    @Override
    public List<StoredFoodResponse> findAllStoredFoods() {
        return storedFoodRepository.findAll()
                .stream()
                .map(StoredFoodMapper::mapToStoredFoodResponse)
                .toList();
    }

    @Override
    public List<StoredFoodResponse> findAllByHomeId(long homeId) {
        return storedFoodRepository.findAll()
                .stream()
                .filter(sf -> sf.getHome().getId() == homeId)
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

        // Home and Food are immutable after creation.
        if (request.storageTypeId() != null) {
            StorageType storageType = storageTypeService.findEntity(request.storageTypeId());
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
            return StoredFoodMapper.mapToStoredFoodResponse(existing); // last known state
        }
        existing.setQuantity(newQty);
        return StoredFoodMapper.mapToStoredFoodResponse(storedFoodRepository.save(existing));
    }

    private StoredFood findEntity(long id) {
        return storedFoodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StoredFood not found with id: " + id));
    }
}
