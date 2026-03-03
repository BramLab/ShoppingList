package be.intecbrussel.shoppinglist.service;

import be.intecbrussel.shoppinglist.dto.StorageTypeMapper;
import be.intecbrussel.shoppinglist.dto.StorageTypeRequest;
import be.intecbrussel.shoppinglist.dto.StorageTypeResponse;
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
    public StorageTypeResponse saveStorageType(StorageTypeRequest request) {
        StorageType saved = storageTypeRepository.save(
                StorageTypeMapper.mapToStorageType(request));
        return StorageTypeMapper.mapToStorageTypeResponse(saved);
    }

    @Override
    public List<StorageTypeResponse> findAllStorageTypes() {
        return storageTypeRepository.findAll()
                .stream()
                .map(StorageTypeMapper::mapToStorageTypeResponse)
                .toList();
    }

    @Override
    public StorageTypeResponse findStorageTypeById(long id) {
        return StorageTypeMapper.mapToStorageTypeResponse(findEntity(id));
    }

    @Override
    public StorageTypeResponse updateStorageType(long id, StorageTypeRequest request) {
        StorageType existing = findEntity(id);
        if (request.name() != null && !request.name().isBlank()) {
            existing.setName(request.name());
        }
        if (request.remarks() != null) {
            existing.setRemarks(request.remarks());
        }
        return StorageTypeMapper.mapToStorageTypeResponse(storageTypeRepository.save(existing));
    }

    @Override
    public void deleteStorageType(long id) {
        findEntity(id);
        storageTypeRepository.deleteById(id);
    }

    // Package-private: lets StoredFoodServiceImpl resolve a StorageType entity.
    StorageType findEntity(long id) {
        return storageTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StorageType not found with id: " + id));
    }
}
