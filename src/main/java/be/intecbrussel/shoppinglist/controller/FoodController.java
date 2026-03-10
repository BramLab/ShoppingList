package be.intecbrussel.shoppinglist.controller;

import be.intecbrussel.shoppinglist.dto.DeletedFoodResponse;
import be.intecbrussel.shoppinglist.dto.FoodOriginalConsumeRequest;
import be.intecbrussel.shoppinglist.dto.FoodOriginalRequest;
import be.intecbrussel.shoppinglist.dto.FoodOriginalResponse;
import be.intecbrussel.shoppinglist.dto.FoodOriginalUpdateRequest;
import be.intecbrussel.shoppinglist.dto.OpenPackageRequest;
import be.intecbrussel.shoppinglist.service.FoodService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/foods")
@RequiredArgsConstructor
public class FoodController {

    private final FoodService foodService;

    /**
     * POST /api/foods
     * Add a new (sealed) food item to the catalogue.
     */
    @PostMapping
    public ResponseEntity<FoodOriginalResponse> createFood(
            @Valid @RequestBody FoodOriginalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(foodService.saveFood(request));
    }

    /**
     * GET /api/foods
     * Returns all active (non-deleted) foods.
     */
    @GetMapping
    public ResponseEntity<List<FoodOriginalResponse>> getAllFoods() {
        return ResponseEntity.ok(foodService.findAllFoods());
    }

    /**
     * GET /api/foods/deleted
     * Returns every soft-deleted food so the UI can display a recycle-bin view.
     *
     * IMPORTANT: this mapping must be declared BEFORE /{id} so Spring does not
     * try to parse "deleted" as a long path variable.
     */
    @GetMapping("/deleted")
    public ResponseEntity<List<DeletedFoodResponse>> getDeletedFoods() {
        return ResponseEntity.ok(foodService.findAllDeletedFoods());
    }

    /**
     * GET /api/foods/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<FoodOriginalResponse> getFoodById(@PathVariable long id) {
        return ResponseEntity.ok(foodService.findFoodById(id));
    }

    /**
     * PATCH /api/foods/{id}
     * Partial update — only non-null fields in the request body are applied.
     */
    @PatchMapping("/{id}")
    public ResponseEntity<FoodOriginalResponse> updateFood(
            @PathVariable long id,
            @RequestBody FoodOriginalUpdateRequest request) {
        return ResponseEntity.ok(foodService.updateFood(id, request));
    }

    /**
     * DELETE /api/foods/{id}
     * Soft-deletes the food item.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFood(@PathVariable long id) {
        foodService.deleteFood(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/foods/{id}/restore
     * Reverses a soft-delete, making the food active again.
     * Returns 404 when no food (active or deleted) exists with the given id.
     */
    @PostMapping("/{id}/restore")
    public ResponseEntity<FoodOriginalResponse> restoreFood(@PathVariable long id) {
        return ResponseEntity.ok(foodService.restoreFood(id));
    }

    /**
     * POST /api/foods/{id}/open
     * Mark a sealed package as opened: sets a shorter useBy date and
     * optionally records an initial serving taken from it.
     */
    @PostMapping("/{id}/open")
    public ResponseEntity<FoodOriginalResponse> openPackage(
            @PathVariable long id,
            @Valid @RequestBody OpenPackageRequest request) {
        return ResponseEntity.ok(foodService.openPackage(id, request));
    }

    /**
     * POST /api/foods/{id}/consume
     * Record that some ml/g was used from an already-opened package.
     */
    @PostMapping("/{id}/consume")
    public ResponseEntity<FoodOriginalResponse> consume(
            @PathVariable long id,
            @Valid @RequestBody FoodOriginalConsumeRequest request) {
        return ResponseEntity.ok(foodService.consume(id, request));
    }
}