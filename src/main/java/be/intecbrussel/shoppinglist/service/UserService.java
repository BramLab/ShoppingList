package be.intecbrussel.shoppinglist.service;

import be.intecbrussel.shoppinglist.dto.UserChangeRoleRequest;
import be.intecbrussel.shoppinglist.dto.UserResponse;

import java.util.List;

public interface UserService {
    List<UserResponse> getAllUsers();
    UserResponse getUserById(long id);
    UserResponse updateUserChangeRole(long id, UserChangeRoleRequest request);
    void deleteUser(long id);
    UserResponse getLoggedInUser();
}
