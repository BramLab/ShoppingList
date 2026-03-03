package be.intecbrussel.shoppinglist.dto;

import be.intecbrussel.shoppinglist.model.StorageType;

public class StorageTypeMapper {

    public static StorageTypeResponse mapToStorageTypeResponse(StorageType storageType) {
        return new StorageTypeResponse(
                storageType.getId(),
                storageType.getName(),
                storageType.getRemarks()
        );
    }

    public static StorageType mapToStorageType(StorageTypeRequest request) {
        StorageType storageType = new StorageType();
        storageType.setName(request.name());
        storageType.setRemarks(request.remarks());
        return storageType;
    }
}
