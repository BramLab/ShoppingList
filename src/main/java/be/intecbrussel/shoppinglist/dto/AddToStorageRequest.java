package be.intecbrussel.shoppinglist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.time.LocalDate;

/**
 * Everything needed to create a {@link be.intecbrussel.shoppinglist.model.FoodOriginal}
 * and immediately put it into storage as a {@link be.intecbrussel.shoppinglist.model.StoredFood}.
 *
 * <h3>Example – adding an opened 1-litre milk carton to the fridge</h3>
 * <pre>
 * {
 *   "name":           "Milk",
 *   "remarks":        "Whole milk 3.5%",
 *   "bestBeforeEnd":  "2026-04-01",
 *   "originalMlG":    1000.0,
 *   "useBy":          "2026-03-12",
 *   "remainingMlG":   800.0,
 *   "homeId":         1,
 *   "storageTypeId":  2,
 *   "quantity":       1
 * }
 * </pre>
 *
 * <h3>Example – adding a sealed 6-pack of yoghurt to the pantry</h3>
 * <pre>
 * {
 *   "name":           "Yoghurt",
 *   "bestBeforeEnd":  "2026-05-10",
 *   "originalMlG":    150.0,
 *   "remainingMlG":   150.0,
 *   "homeId":         1,
 *   "storageTypeId":  3,
 *   "quantity":       6
 * }
 * </pre>
 */
@Data
public class AddToStorageRequest {

    // ── FoodOriginal fields ────────────────────────────────────────────────────

    @NotBlank
    private String name;

    private String remarks;

    /** Sealed best-before date (printed on the packaging). */
    private LocalDate bestBeforeEnd;

    /** Total content of one sealed unit in ml or g. */
    @NotNull
    @Positive
    private Double originalMlG;

    /**
     * How much is left in this unit right now.
     * Defaults to {@code originalMlG} when null (i.e. the unit is still sealed).
     */
    @PositiveOrZero
    private Double remainingMlG;

    /**
     * Use-by date once opened – only relevant when the unit is already open
     * ({@code remainingMlG < originalMlG}).
     */
    private LocalDate useBy;

    // ── StoredFood fields ──────────────────────────────────────────────────────

    @NotNull
    private Long homeId;

    @NotNull
    private Long storageTypeId;

    /**
     * How many identical sealed units are stored together.
     * Defaults to 1 when not supplied.
     */
    @Positive
    private int quantity = 1;
}
