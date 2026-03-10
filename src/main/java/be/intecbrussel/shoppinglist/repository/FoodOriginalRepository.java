package be.intecbrussel.shoppinglist.repository;

import be.intecbrussel.shoppinglist.model.FoodOriginal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FoodOriginalRepository extends JpaRepository<FoodOriginal, Long> {

    /**
     * Returns every soft-deleted FoodOriginal row.
     *
     * Hibernate's @SoftDelete filter silently excludes deleted rows from all
     * JPQL / entity queries, so a native SQL query is required here to bypass it.
     *
     * Column aliases must match FoodDeletedView getter names (case-insensitive).
     */
    @Query(value = """
            SELECT
                f.id                        AS id,
                f.name                      AS name,
                f.remarks                   AS remarks,
                fo.best_before_end          AS bestBeforeEnd,
                fo.original_ml_g            AS original_ml_g,
                fo.use_by                   AS useBy,
                fo.remaining_ml_g           AS remaining_ml_g,
                f.updated_at                AS updatedAt,
                COALESCE(SUM(sf.quantity), 0) AS quantity
            FROM food f
            JOIN food_original fo ON f.id = fo.food_id
            LEFT JOIN stored_food sf ON sf.food_id = f.id
            WHERE f.deleted_at IS NOT NULL
            GROUP BY f.id, f.name, f.remarks, fo.best_before_end,
                     fo.original_ml_g, fo.use_by, fo.remaining_ml_g, f.updated_at
            ORDER BY f.deleted_at DESC
            """, nativeQuery = true)
    List<FoodDeletedView> findAllDeleted();

    /**
     * Checks whether a food row (active or deleted) exists with this id.
     * Used by restore to give a proper 404 when the id is unknown.
     */
    @Query(value = "SELECT COUNT(*) FROM food WHERE id = :id", nativeQuery = true)
    int countIncludingDeleted(@Param("id") long id);

    /**
     * Clears deleted_at, making the food active again.
     *
     * clearAutomatically = true  — evicts all entities from the first-level cache
     *   after the UPDATE so that any subsequent findById in the same transaction
     *   re-reads from the database rather than returning the stale cached state.
     *
     * flushAutomatically = true  — flushes any pending changes before the UPDATE
     *   so the write order is deterministic.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE food SET deleted_at = NULL WHERE id = :id", nativeQuery = true)
    void restoreById(@Param("id") long id);

    /**
     * Soft-deletes a FoodOriginal by setting deleted_at directly via native SQL.
     *
     * This bypasses Hibernate's entity lifecycle (@SoftDelete / repository.delete())
     * which would mark the entity as REMOVED. A REMOVED entity referenced by any
     * still-MANAGED entity (e.g. StoredFood.food) causes a
     * TransientPropertyValueException or AssertionFailure at flush/commit time.
     *
     * By issuing the UPDATE directly the entity stays MANAGED in the session,
     * the FK in StoredFood remains valid, and no lifecycle error occurs.
     *
     * flushAutomatically = true ensures any pending INSERTs (e.g. the StoredFood row)
     * are written before this UPDATE runs, preserving a consistent write order.
     */
    @Modifying(flushAutomatically = true)
    @Query(value = "UPDATE food SET deleted_at = current_timestamp(6) WHERE id = :id AND deleted_at IS NULL",
            nativeQuery = true)
    void softDeleteById(@Param("id") long id);
}