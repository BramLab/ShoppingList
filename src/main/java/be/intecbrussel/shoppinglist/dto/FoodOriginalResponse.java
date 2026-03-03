package be.intecbrussel.shoppinglist.dto;

import java.time.LocalDate;

public record FoodOriginalResponse(
        long id,
        String name,
        String remarks,
        LocalDate bestBeforeEnd,
        double original_ml_g,
        LocalDate useBy,
        double remaining_ml_g,

        /**
         * Convenience field: useBy if set, otherwise bestBeforeEnd.
         * Mirrors FoodOriginal#getUseByElseBestBeforeEnd().
         */
        LocalDate effectiveUseBy,

        /** True when remaining_ml_g == -1, meaning the package is empty. */
        boolean empty
) {}
