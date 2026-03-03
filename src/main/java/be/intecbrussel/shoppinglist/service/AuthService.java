package be.intecbrussel.shoppinglist.service;

import be.intecbrussel.shoppinglist.dto.*;

// create, update, delete, getById, getAll, (getByRole)
public interface AuthService {
    UserResponse registerUser(RegisterRequest registerRequest);
    LoginAuthResponse login(LoginAuthRequest loginAuthRequest);
}
