package be.intecbrussel.shoppinglist.repository;

import be.intecbrussel.shoppinglist.model.StoredFood;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoredFoodRepository extends JpaRepository<StoredFood, Long> {

    /**
     * Returns all StoredFood entries for a given home.
     *
     * LEFT JOIN FETCH is required here instead of the derived-query default.
     *
     * With Hibernate 6 + @SoftDelete on a JOINED-inheritance entity, a derived
     * query (`findAllByHomeId`) resolves the @ManyToOne food association with a
     * separate per-row SELECT that includes `food.deleted_at IS NULL` in its WHERE
     * clause. This means:
     *   - Deleted foods: association resolves to null (soft-delete filter excludes them)
     *   - Restored foods: should resolve normally, BUT if the persistence context still
     *     holds a stale cached reference the resolved value may still be null
     *
     * Using LEFT JOIN FETCH forces Hibernate to resolve the food in the SAME query
     * as the StoredFood rows, placing the @SoftDelete restriction inside the JOIN
     * ON condition rather than a separate WHERE clause.  This means:
     *   - Active/restored foods  → joined normally, food is populated       ✓
     *   - Soft-deleted foods     → LEFT JOIN returns null columns, food=null ✓
     *   - The StoredFood row itself is ALWAYS returned regardless              ✓
     */
    @Query("""
            SELECT sf FROM StoredFood sf
            LEFT JOIN FETCH sf.food
            WHERE sf.home.id = :homeId
            """)
    List<StoredFood> findAllByHomeId(@Param("homeId") long homeId);

    /**
     * Same reasoning as findAllByHomeId — uses LEFT JOIN FETCH to ensure
     * all StoredFood rows are returned regardless of the food's soft-delete state.
     */
    @Query("""
            SELECT sf FROM StoredFood sf
            LEFT JOIN FETCH sf.food
            """)
    List<StoredFood> findAllWithFood();
}