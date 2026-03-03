package be.intecbrussel.shoppinglist.service;

import be.intecbrussel.shoppinglist.dto.UserChangeRoleRequest;
import be.intecbrussel.shoppinglist.dto.UserResponse;
import be.intecbrussel.shoppinglist.model.User;

import java.util.List;

public interface UserService {
    List<UserResponse> getAllUsers();
    UserResponse getUserById(long id);
    UserResponse updateUserChangeRole(long id, UserChangeRoleRequest request);
    void deleteUser(long id);

    /** Returns the full User entity for the currently authenticated principal. */
    User getLoggedInUser();
}
