package be.intecbrussel.shoppinglist.dto;

import jakarta.validation.constraints.NotBlank;

public record StorageTypeRequest(
        @NotBlank(message = "StorageType name is required")
        String name,

        String remarks
) {}
