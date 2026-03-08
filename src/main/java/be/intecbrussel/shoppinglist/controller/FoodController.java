package be.intecbrussel.shoppinglist.controller;

import be.intecbrussel.shoppinglist.dto.FoodOriginalConsumeRequest;
import be.intecbrussel.shoppinglist.dto.FoodOriginalRequest;
import be.intecbrussel.shoppinglist.dto.FoodOriginalResponse;
import be.intecbrussel.shoppinglist.dto.FoodOriginalUpdateRequest;
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
     */
    @GetMapping
    public ResponseEntity<List<FoodOriginalResponse>> getAllFoods() {
        return ResponseEntity.ok(foodService.findAllFoods());
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
     * POST /api/foods/{id}/open
     * Mark a sealed package as opened: sets a shorter useBy date and
     * optionally records an initial serving taken from it.
     *
     * Body: { "useBy": "2026-03-10", "initialConsumption": 50 }
     */
//    @PostMapping("/{id}/open")
//    public ResponseEntity<FoodOriginalResponse> openPackage(
//            @PathVariable long id,
//            @Valid @RequestBody OpenPackageRequest request) {
//        return ResponseEntity.ok(foodService.openPackage(id, request));
//    }

    /**
     * POST /api/foods/{id}/consume
     * Record that some ml_g_left was used from an opened package.
     * Soft-deletes the item automatically when remaining reaches 0.
     *
     * Body: { "ml_g_left": 100 }
     */
    @PostMapping("/{id}/consume")
    public ResponseEntity<FoodOriginalResponse> consume(
            @PathVariable long id,
            @Valid @RequestBody FoodOriginalConsumeRequest request) {
        return ResponseEntity.ok(foodService.consume(id, request));
    }
}
