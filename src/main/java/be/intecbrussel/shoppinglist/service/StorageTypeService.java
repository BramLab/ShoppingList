package be.intecbrussel.shoppinglist.service;

import be.intecbrussel.shoppinglist.dto.StorageTypeRequest;
import be.intecbrussel.shoppinglist.dto.StorageTypeResponse;

import java.util.List;

public interface StorageTypeService {
    StorageTypeResponse saveStorageType(StorageTypeRequest request);
    List<StorageTypeResponse> findAllStorageTypes();
    StorageTypeResponse findStorageTypeById(long id);
    StorageTypeResponse updateStorageType(long id, StorageTypeRequest request);
    void deleteStorageType(long id);
}
