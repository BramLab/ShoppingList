package be.intecbrussel.shoppinglist.controller;

import be.intecbrussel.shoppinglist.dto.*;
import be.intecbrussel.shoppinglist.service.StoredFoodService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stored-foods")
@RequiredArgsConstructor
public class StoredFoodController {

    private final StoredFoodService storedFoodService;

    /**
     * POST /api/stored-foods
     * Link an existing food item to a home and a storage location.
     */
    @PostMapping
    public ResponseEntity<StoredFoodResponse> createStoredFood(
            @Valid @RequestBody StoredFoodRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(storedFoodService.saveStoredFood(request));
    }

    /**
     * GET /api/stored-foods
     * Returns every stored-food entry across all homes.
     * Use the homeId query param to filter to a single home.
     *
     * GET /api/stored-foods?homeId=1
     */
    @GetMapping
    public ResponseEntity<List<StoredFoodResponse>> getAllStoredFoods(
            @RequestParam(required = false) Long homeId) {
        if (homeId != null) {
            return ResponseEntity.ok(storedFoodService.findAllByHomeId(homeId));
        }
        return ResponseEntity.ok(storedFoodService.findAllStoredFoods());
    }

    /**
     * GET /api/stored-foods/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<StoredFoodResponse> getStoredFoodById(@PathVariable long id) {
        return ResponseEntity.ok(storedFoodService.findStoredFoodById(id));
    }

    /**
     * PATCH /api/stored-foods/{id}
     * Change the storage location and/or quantity directly.
     * Home and food are immutable after creation.
     *
     * Body (all fields optional): { "storageTypeId": 2, "quantity": 5 }
     */
    @PatchMapping("/{id}")
    public ResponseEntity<StoredFoodResponse> updateStoredFood(
            @PathVariable long id,
            @Valid @RequestBody StoredFoodUpdateRequest request) {
        return ResponseEntity.ok(storedFoodService.updateStoredFood(id, request));
    }

    /**
     * DELETE /api/stored-foods/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStoredFood(@PathVariable long id) {
        storedFoodService.deleteStoredFood(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * PATCH /api/stored-foods/{id}/quantity
     * Adjust the quantity by a delta (positive = restock, negative = consume from stock).
     * Automatically removes the entry when quantity reaches 0.
     *
     * Body: { "delta": -1 }
     */
    @PatchMapping("/{id}/quantity")
    public ResponseEntity<StoredFoodResponse> adjustQuantity(
            @PathVariable long id,
            @Valid @RequestBody AdjustQuantityRequest request) {
        return ResponseEntity.ok(storedFoodService.adjustQuantity(id, request));
    }


    /**
     * POST /stored-foods/{id}/consume
     *
     * Take one unit out of a pack.
     *
     * Request body example:
     * <pre>
     * {
     *   "remainingMlG": 1000.0,
     *   "useBy": "2026-03-20",
     *   "storageTypeId": null
     * }
     * </pre>
     *
     * Response body example (success):
     * <pre>
     * {
     *   "sourceStoredFoodId": 5,
     *   "sourceRemainingQuantity": 11,
     *   "openedStoredFoodId": 42,
     *   "openedRemainingMlG": 1000.0,
     *   "openedUnitWasEmpty": false
     * }
     * </pre>
     */
    @PostMapping("/{id}/consume")
    public ResponseEntity<ConsumeResult> consume(
            @PathVariable long id,
            @Valid @RequestBody ConsumeRequest request) {

        ConsumeResult result = storedFoodService.consume(id, request);
        return ResponseEntity.ok(result);
    }

}
