package be.intecbrussel.shoppinglist.model;

import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Data //Combines @Getter, @Setter, @ToString, @EqualsAndHashCode, @RequiredArgsConstructor
@ToString(callSuper=true)
@Entity
public class StoredFood extends AuditModel {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private long id;

    // As a general rule, cascade=CascadeType.ALL (or any cascade) on a @ManyToOne is almost never correct.
    // Cascading makes sense on @OneToMany — flowing from parent down to children — not from a child up to its parent.
    // You wouldn't want deleting a StoredFood to also delete the Home or the Food item itself.
    @ManyToOne(optional = false)
    Home home;

    @ManyToOne(optional = false)
    Food food;

    @ManyToOne(optional = false)
    StorageType storageType;

    int quantity;

}
