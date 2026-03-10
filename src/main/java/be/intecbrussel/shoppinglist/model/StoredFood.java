package be.intecbrussel.shoppinglist.model;

import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@Entity
public class StoredFood extends AuditModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(optional = false)
    Home home;

    /**
     * LEFT JOIN (optional = true, the default) instead of INNER JOIN.
     *
     * Hibernate's @SoftDelete filter on Food adds "food.deleted_at IS NULL" to the
     * JOIN condition. With INNER JOIN (optional = false) a StoredFood row whose food
     * is soft-deleted is silently excluded from every query result — including after
     * the food is restored, because by that point the row is missing from the
     * result set and never re-evaluated.
     *
     * With LEFT JOIN, the row is always returned. When the food is soft-deleted,
     * getFood() returns null and the mapper renders the entry without food details.
     * Once the food is restored (deleted_at = NULL), getFood() returns the entity
     * normally and the full entry appears in the Stored Foods table again.
     */
    @ManyToOne(optional = true)
    Food food;

    @ManyToOne(optional = false)
    StorageType storageType;

    int quantity;
}