package be.intecbrussel.shoppinglist.dto;

import jakarta.validation.constraints.NotNull;
import be.intecbrussel.shoppinglist.model.Role;

public record UserChangeRoleRequest(
    @NotNull(message = "Id is required")
    long id,

    @NotNull(message = "Role is required")
    Role role

) {}
