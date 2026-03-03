package be.intecbrussel.shoppinglist.controller;

import be.intecbrussel.shoppinglist.dto.UserChangeRoleRequest;
import be.intecbrussel.shoppinglist.dto.UserResponse;
import be.intecbrussel.shoppinglist.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ── Authenticated user ────────────────────────────────────────────────────

    /**
     * GET /api/users/me
     * Returns the profile of the currently logged-in user.
     */
    @GetMapping("/api/users/me")
    public ResponseEntity<UserResponse> getMe() {
        return ResponseEntity.ok(userService.getLoggedInUser());
    }

    // ── Admin-only endpoints (/api/admin/** is secured in SecurityConfig) ─────

    /**
     * GET /api/admin/users
     */
    @GetMapping("/api/admin/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * GET /api/admin/users/{id}
     */
    @GetMapping("/api/admin/users/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    /**
     * PATCH /api/admin/users/{id}/role
     * Change the role of any user.
     *
     * Body: { "id": 1, "role": "ADMIN" }
     */
    @PatchMapping("/api/admin/users/{id}/role")
    public ResponseEntity<UserResponse> changeRole(
            @PathVariable long id,
            @Valid @RequestBody UserChangeRoleRequest request) {
        return ResponseEntity.ok(userService.updateUserChangeRole(id, request));
    }

    /**
     * DELETE /api/admin/users/{id}
     */
    @DeleteMapping("/api/admin/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
