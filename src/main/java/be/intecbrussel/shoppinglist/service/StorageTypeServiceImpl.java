package be.intecbrussel.shoppinglist.service;

import be.intecbrussel.shoppinglist.exception.MissingDataException;
import be.intecbrussel.shoppinglist.exception.ResourceNotFoundException;
import be.intecbrussel.shoppinglist.model.StorageType;
import be.intecbrussel.shoppinglist.repository.StorageTypeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class StorageTypeServiceImpl implements StorageTypeService {

    private final StorageTypeRepository storageTypeRepository;

    @Override
    public StorageType saveStorageType(StorageType storageType) {
        if (storageType.getName() == null || storageType.getName().isBlank()) {
            throw new MissingDataException("StorageType name is required");
        }
        return storageTypeRepository.save(storageType);
    }

    @Override
    public List<StorageType> findAllStorageTypes() {
        return storageTypeRepository.findAll();
    }

    @Override
    public StorageType findStorageTypeById(long id) {
        return storageTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StorageType not found with id: " + id));
    }

    @Override
    public StorageType updateStorageType(StorageType incoming, long id) {
        StorageType existing = findStorageTypeById(id);
        if (incoming.getName() != null && !incoming.getName().isBlank()) {
            existing.setName(incoming.getName());
        }
        if (incoming.getRemarks() != null) {
            existing.setRemarks(incoming.getRemarks());
        }
        return storageTypeRepository.save(existing);
    }

    @Override
    public void deleteStorageType(long id) {
        findStorageTypeById(id);
        storageTypeRepository.deleteById(id);
    }
}
