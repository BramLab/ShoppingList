package be.intecbrussel.shoppinglist.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import be.intecbrussel.shoppinglist.dto.*;
import be.intecbrussel.shoppinglist.security.JwtUtil;
import be.intecbrussel.shoppinglist.exception.DuplicateEnrollmentException;
import be.intecbrussel.shoppinglist.exception.MissingDataException;
import be.intecbrussel.shoppinglist.exception.ResourceNotFoundException;
import be.intecbrussel.shoppinglist.model.Home;
import be.intecbrussel.shoppinglist.model.User;
import be.intecbrussel.shoppinglist.repository.UserHomeRepository;
import be.intecbrussel.shoppinglist.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserHomeRepository userHomeRepository;   // needed to persist the new Home
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public UserResponse registerUser(RegisterRequest registerRequest) {
        User newUser = new User();

        // ── Validate & set username ───────────────────────────────────────────────
        if (registerRequest.username().isBlank()) {
            throw new MissingDataException("Username is required");
        } else if (userRepository.findByUsername(registerRequest.username()).isPresent()) {
            throw new DuplicateEnrollmentException("Username already exists.");
        } else {
            newUser.setUsername(registerRequest.username());
        }

        // ── Validate & set email ──────────────────────────────────────────────────
        if (registerRequest.email().isBlank()) {
            throw new MissingDataException("Email is required");
        } else if (userRepository.findByEmail(registerRequest.email()).isPresent()) {
            throw new DuplicateEnrollmentException("Email already exists.");
        } else {
            newUser.setEmail(registerRequest.email());
        }

        // ── Role ──────────────────────────────────────────────────────────────────
        if (registerRequest.role() == null) {
            throw new MissingDataException("Role is required");
        } else {
            newUser.setRole(registerRequest.role());
        }

        // ── Password ──────────────────────────────────────────────────────────────
        if (registerRequest.password() == null || registerRequest.password().length() < 8) {
            throw new MissingDataException("Password is required and should be at least 8 characters long");
        } else {
            newUser.setPasswordHashed(passwordEncoder.encode(registerRequest.password()));
        }

        // ── Create a home for this user (1 user → 1 home on registration) ────────
        if (registerRequest.homeName() == null || registerRequest.homeName().isBlank()) {
            throw new MissingDataException("Home name is required");
        }
        Home home = new Home();
        home.setName(registerRequest.homeName());
        Home savedHome = userHomeRepository.save(home);
        newUser.setHome(savedHome);

        // ── Persist ───────────────────────────────────────────────────────────────
        User createdUser = userRepository.save(newUser);
        return UserMapper.mapToUserResponse(createdUser);
    }

    @Override
    public LoginAuthResponse login(LoginAuthRequest loginAuthRequest) {
        Optional<User> optionalUser = userRepository.findByUsername(loginAuthRequest.username());
        if (optionalUser.isEmpty()) {
            throw new ResourceNotFoundException("Username not found. Are you registered?");
        }
        User user = optionalUser.get();

        if (!passwordEncoder.matches(loginAuthRequest.password(), user.getPasswordHashed())) {
            throw new ResourceNotFoundException("Invalid password");
        }

        String jwtToken = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
        return new LoginAuthResponse(user.getId(), user.getUsername(), user.getEmail(),
                user.getRole(), jwtToken);
    }
}