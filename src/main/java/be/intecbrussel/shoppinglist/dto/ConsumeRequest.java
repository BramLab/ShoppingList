package be.intecbrussel.shoppinglist.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.time.LocalDate;

/**
 * Payload for the "consume one unit from a StoredFood pack" operation.
 *
 * <ul>
 *   <li>{@code remainingMlG}  – how much product is left in the opened unit (0 = empty → soft-delete).</li>
 *   <li>{@code useBy}         – optional date by which the opened unit should be used.</li>
 *   <li>{@code storageTypeId} – optional override; if null the parent StoredFood's StorageType is reused.</li>
 * </ul>
 */
@Data
public class ConsumeRequest {

    /** How much is left in the opened unit. Pass 0 to immediately mark it as empty (soft-delete). */
    @NotNull
    @PositiveOrZero
    private Double remainingMlG;

    /** Use-by date for the opened unit (may differ from the sealed best-before). */
    private LocalDate useBy;

    /**
     * Override the storage type for the opened unit (e.g. move from "pantry" to "fridge").
     * When null the parent StoredFood's storage type is inherited.
     */
    private Long storageTypeId;
}
