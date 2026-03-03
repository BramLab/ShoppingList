package be.intecbrussel.shoppinglist.controller;

import be.intecbrussel.shoppinglist.dto.StorageTypeRequest;
import be.intecbrussel.shoppinglist.dto.StorageTypeResponse;
import be.intecbrussel.shoppinglist.service.StorageTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/storage-types")
@RequiredArgsConstructor
public class StorageTypeController {

    private final StorageTypeService storageTypeService;

    /**
     * POST /api/storage-types
     */
    @PostMapping
    public ResponseEntity<StorageTypeResponse> createStorageType(
            @Valid @RequestBody StorageTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(storageTypeService.saveStorageType(request));
    }

    /**
     * GET /api/storage-types
     */
    @GetMapping
    public ResponseEntity<List<StorageTypeResponse>> getAllStorageTypes() {
        return ResponseEntity.ok(storageTypeService.findAllStorageTypes());
    }

    /**
     * GET /api/storage-types/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<StorageTypeResponse> getStorageTypeById(@PathVariable long id) {
        return ResponseEntity.ok(storageTypeService.findStorageTypeById(id));
    }

    /**
     * PUT /api/storage-types/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<StorageTypeResponse> updateStorageType(
            @PathVariable long id,
            @Valid @RequestBody StorageTypeRequest request) {
        return ResponseEntity.ok(storageTypeService.updateStorageType(id, request));
    }

    /**
     * DELETE /api/storage-types/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStorageType(@PathVariable long id) {
        storageTypeService.deleteStorageType(id);
        return ResponseEntity.noContent().build();
    }
}
