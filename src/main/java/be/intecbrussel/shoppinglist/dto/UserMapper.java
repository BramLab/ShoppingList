package be.intecbrussel.shoppinglist.dto;

import be.intecbrussel.shoppinglist.model.User;

public class UserMapper {

    public static UserResponse mapToUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole()
        );
    }

}
