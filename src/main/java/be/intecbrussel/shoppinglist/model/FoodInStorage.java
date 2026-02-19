package be.intecbrussel.shoppinglist.model;

import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data //Combines @Getter, @Setter, @ToString, @EqualsAndHashCode, @RequiredArgsConstructor
@ToString(callSuper=true)
@Entity
public class FoodInStorage {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private long id;

    @ManyToOne(cascade=CascadeType.ALL)
    Food food;

    @ManyToOne(cascade=CascadeType.ALL)
    Storage storage;

    int quantity;

}
