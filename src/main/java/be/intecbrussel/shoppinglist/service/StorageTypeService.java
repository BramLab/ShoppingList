package be.intecbrussel.shoppinglist.service;

import be.intecbrussel.shoppinglist.model.StorageType;

import java.util.List;

public interface StorageTypeService {
    StorageType saveStorageType(StorageType storageType);
    List<StorageType> findAllStorageTypes();
    StorageType findStorageTypeById(long id);
    StorageType updateStorageType(StorageType storageType, long id);
    void deleteStorageType(long id);
}
