package be.intecbrussel.shoppinglist.controller;

import be.intecbrussel.shoppinglist.dto.LoginAuthRequest;
import be.intecbrussel.shoppinglist.dto.LoginAuthResponse;
import be.intecbrussel.shoppinglist.dto.RegisterRequest;
import be.intecbrussel.shoppinglist.dto.UserResponse;
import be.intecbrussel.shoppinglist.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/auth/register
     * Public — creates a new user together with their own home.
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = authService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * POST /api/auth/login
     * Public — returns a JWT on success.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginAuthResponse> login(@Valid @RequestBody LoginAuthRequest request) {
        LoginAuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
