package be.intecbrussel.shoppinglist.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.time.LocalDate;

/**
 * Payload for the "consume one or more units from a StoredFood pack" operation.
 *
 * <ul>
 *   <li>{@code quantity}      – how many sealed units to take out of the pack (default 1).</li>
 *   <li>{@code remainingMlG}  – how much product is left in each opened unit (0 = empty → soft-delete).</li>
 *   <li>{@code useBy}         – optional date by which the opened units should be used.</li>
 *   <li>{@code storageTypeId} – optional override; if null the parent StoredFood's StorageType is reused.</li>
 * </ul>
 */
@Data
public class ConsumeRequest {

    /**
     * Number of sealed units to take out of the pack.
     * Defaults to 1. Must not exceed the current StoredFood quantity.
     */
    @Positive(message = "quantity must be at least 1")
    private int quantity = 1;

    /** How much is left in each opened unit. Pass 0 to immediately mark them as empty (soft-delete). */
    @NotNull
    @PositiveOrZero
    private Double remainingMlG;

    /** Use-by date for the opened units (may differ from the sealed best-before). */
    private LocalDate useBy;

    /**
     * Override the storage type for the opened units (e.g. move from "pantry" to "fridge").
     * When null the parent StoredFood's storage type is inherited.
     */
    private Long storageTypeId;
}