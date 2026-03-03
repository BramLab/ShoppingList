package be.intecbrussel.shoppinglist.dto;

import be.intecbrussel.shoppinglist.model.Role;

public record UserResponse(
        long id,
        String userName,
        String email,
        Role role,
        /** ID of the home that was created (or already assigned) for this user. */
        long homeId
) {}
