package be.intecbrussel.shoppinglist.controller;

import be.intecbrussel.shoppinglist.dto.HomeRequest;
import be.intecbrussel.shoppinglist.dto.HomeResponse;
import be.intecbrussel.shoppinglist.service.HomeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/homes")
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;

    /**
     * POST /api/homes
     * Create a standalone home (useful for adding a second home to an existing user).
     */
    @PostMapping
    public ResponseEntity<HomeResponse> createHome(@Valid @RequestBody HomeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(homeService.saveHome(request));
    }

    /**
     * GET /api/homes
     */
    @GetMapping
    public ResponseEntity<List<HomeResponse>> getAllHomes() {
        return ResponseEntity.ok(homeService.findAllHomes());
    }

    /**
     * GET /api/homes/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<HomeResponse> getHomeById(@PathVariable long id) {
        return ResponseEntity.ok(homeService.findHomeById(id));
    }

    /**
     * PUT /api/homes/{id}
     * Rename a home.
     */
    @PutMapping("/{id}")
    public ResponseEntity<HomeResponse> updateHome(
            @PathVariable long id,
            @Valid @RequestBody HomeRequest request) {
        return ResponseEntity.ok(homeService.updateHome(id, request));
    }

    /**
     * DELETE /api/homes/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHome(@PathVariable long id) {
        homeService.deleteHome(id);
        return ResponseEntity.noContent().build();
    }
}
