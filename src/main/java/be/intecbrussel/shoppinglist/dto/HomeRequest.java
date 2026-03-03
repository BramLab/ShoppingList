package be.intecbrussel.shoppinglist.dto;

import jakarta.validation.constraints.NotBlank;

public record HomeRequest(
        @NotBlank(message = "Home name is required")
        String name
) {}
