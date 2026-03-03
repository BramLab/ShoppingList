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
        return UserMapper.mapToUserResponse(findEntity(id));
    }

    @Override
    public UserResponse updateUserChangeRole(long id, UserChangeRoleRequest request) {
        User user = findEntity(id);
        user.setRole(request.role());
        return UserMapper.mapToUserResponse(userRepository.save(user));
    }

    @Override
    public void deleteUser(long id) {
        findEntity(id);
        userRepository.deleteById(id);
    }

    @Override
    public UserResponse getLoggedInUser() {
        String username;
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            username = auth.getName();
        } catch (Exception e) {
            throw new UnauthorizedActionException("You are not authenticated. Please log in.");
        }
        return UserMapper.mapToUserResponse(
                userRepository.findByUsername(username)
                        .orElseThrow(() -> new UnauthorizedActionException(
                                "Authenticated user not found in database.")));
    }

    private User findEntity(long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }
}
