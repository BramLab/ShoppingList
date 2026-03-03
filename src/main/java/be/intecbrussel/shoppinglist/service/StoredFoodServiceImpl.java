package be.intecbrussel.shoppinglist.service;

import be.intecbrussel.shoppinglist.exception.MissingDataException;
import be.intecbrussel.shoppinglist.exception.ResourceNotFoundException;
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
    private final HomeService homeService; // for existence check

    @Override
    public StoredFood saveStoredFood(StoredFood storedFood) {
        if (storedFood.getHome() == null) {
            throw new MissingDataException("Home is required for a StoredFood entry");
        }
        if (storedFood.getFood() == null) {
            throw new MissingDataException("Food is required for a StoredFood entry");
        }
        if (storedFood.getStorageType() == null) {
            throw new MissingDataException("StorageType is required for a StoredFood entry");
        }
        if (storedFood.getQuantity() < 0) {
            throw new MissingDataException("Quantity cannot be negative");
        }
        return storedFoodRepository.save(storedFood);
    }

    @Override
    public List<StoredFood> findAllStoredFoods() {
        return storedFoodRepository.findAll();
    }

    @Override
    public List<StoredFood> findAllByHomeId(long homeId) {
        homeService.findHome(homeId); // verify home exists
        return storedFoodRepository.findAll()
                .stream()
                .filter(sf -> sf.getHome().getId() == homeId)
                .toList();
    }

    @Override
    public StoredFood findStoredFoodById(long id) {
        return storedFoodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StoredFood not found with id: " + id));
    }

    @Override
    public StoredFood updateStoredFood(StoredFood incoming, long id) {
        StoredFood existing = findStoredFoodById(id);

        if (incoming.getStorageType() != null) {
            existing.setStorageType(incoming.getStorageType());
        }
        if (incoming.getQuantity() >= 0) {
            existing.setQuantity(incoming.getQuantity());
        }
        // Home and Food are immutable after creation — moving food belongs to a new entry.
        return storedFoodRepository.save(existing);
    }

    @Override
    public void deleteStoredFood(long id) {
        findStoredFoodById(id);
        storedFoodRepository.deleteById(id);
    }

    @Override
    public StoredFood adjustQuantity(long id, int delta) {
        StoredFood existing = findStoredFoodById(id);
        int newQty = existing.getQuantity() + delta;
        if (newQty < 0) {
            throw new MissingDataException("Quantity cannot drop below 0 (current: "
                    + existing.getQuantity() + ", delta: " + delta + ")");
        }
        if (newQty == 0) {
            storedFoodRepository.deleteById(id);
            return existing; // return last known state
        }
        existing.setQuantity(newQty);
        return storedFoodRepository.save(existing);
    }
}
