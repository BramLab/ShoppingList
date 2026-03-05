package be.intecbrussel.shoppinglist.dto;

import be.intecbrussel.shoppinglist.model.Role;

public record LoginAuthResponse(
        long id,
        String username,
        String email,
        Role role,
        String token
) {}
