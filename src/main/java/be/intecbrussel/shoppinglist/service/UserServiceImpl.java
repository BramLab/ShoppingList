package be.intecbrussel.shoppinglist.service;

import be.intecbrussel.shoppinglist.dto.UserChangeRoleRequest;
import be.intecbrussel.shoppinglist.dto.UserMapper;
import be.intecbrussel.shoppinglist.dto.UserResponse;
import be.intecbrussel.shoppinglist.exception.ResourceNotFoundException;
import be.intecbrussel.shoppinglist.exception.UnauthorizedActionException;
import be.intecbrussel.shoppinglist.model.User;
import be.intecbrussel.shoppinglist.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserMapper::mapToUserResponse)
                .toList();
    }

    @Override
    public UserResponse getUserById(long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return UserMapper.mapToUserResponse(user);
    }

    @Override
    public UserResponse updateUserChangeRole(long id, UserChangeRoleRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        user.setRole(request.role());
        return UserMapper.mapToUserResponse(userRepository.save(user));
    }

    @Override
    public void deleteUser(long id) {
        userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        userRepository.deleteById(id);
    }

    @Override
    public User getLoggedInUser() {
        String username;
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            username = authentication.getName();
        } catch (Exception e) {
            throw new UnauthorizedActionException("You are not authenticated. Please log in.");
        }

        Optional<User> optionalUser = userRepository.findByUsername(username);
        return optionalUser
                .orElseThrow(() -> new UnauthorizedActionException("Authenticated user not found in database."));
    }
}
