package be.intecbrussel.shoppinglist.repository;

import java.time.LocalDate;
import java.util.Date;

/**
 * Spring Data projection — maps columns returned by the native "find deleted foods" query.
 * Column aliases in the query must match getter names (case-insensitive, minus "get").
 */
public interface FoodDeletedView {
    long      getId();
    String    getName();
    String    getRemarks();
    LocalDate getBestBeforeEnd();
    double    getOriginal_ml_g();
    LocalDate getUseBy();
    double    getRemaining_ml_g();
    Date      getUpdatedAt();

    /**
     * Total quantity across all StoredFood rows that reference this food.
     * Reflects how many units were consumed/marked empty in one operation.
     * Returns 0 when no StoredFood row exists (food was deleted without going through consume).
     */
    int getQuantity();
}