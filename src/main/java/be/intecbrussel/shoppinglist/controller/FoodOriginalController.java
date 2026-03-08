package be.intecbrussel.shoppinglist.controller;

import be.intecbrussel.shoppinglist.dto.AddToStorageRequest;
import be.intecbrussel.shoppinglist.dto.AddToStorageResult;
import be.intecbrussel.shoppinglist.service.FoodOriginalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/food-originals")   // was "/food-originals" — outside security rules
@RequiredArgsConstructor
public class FoodOriginalController {

    private final FoodOriginalService foodOriginalService;

    /**
     * POST /api/food-originals
     *
     * Create a FoodOriginal and register it in storage in one step.
     *
     * <h3>Request body – sealed 6-pack of yoghurt</h3>
     * <pre>
     * {
     *   "name":          "Yoghurt",
     *   "remarks":       "Strawberry flavour",
     *   "bestBeforeEnd": "2026-05-10",
     *   "originalMlG":   150.0,
     *   "homeId":        1,
     *   "storageTypeId": 3,
     *   "quantity":      6
     * }
     * </pre>
     *
     * <h3>Request body – already-opened milk carton</h3>
     * <pre>
     * {
     *   "name":          "Milk",
     *   "originalMlG":   1000.0,
     *   "remainingMlG":  600.0,
     *   "useBy":         "2026-03-12",
     *   "homeId":        1,
     *   "storageTypeId": 2,
     *   "quantity":      1
     * }
     * </pre>
     *
     * <h3>Response – 201 Created</h3>
     * <pre>
     * {
     *   "foodOriginalId": 42,
     *   "storedFoodId":   17,
     *   "quantity":       6,
     *   "remainingMlG":   150.0
     * }
     * </pre>
     */
    @PostMapping
    public ResponseEntity<AddToStorageResult> addToStorage(
            @Valid @RequestBody AddToStorageRequest request) {

        AddToStorageResult result = foodOriginalService.addToStorage(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(result.getFoodOriginalId())
                .toUri();

        return ResponseEntity.created(location).body(result);
    }
}
