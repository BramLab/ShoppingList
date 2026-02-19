package be.intecbrussel.shoppinglist.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data //Combines @Getter, @Setter, @ToString, @EqualsAndHashCode, @RequiredArgsConstructor
@ToString(callSuper=true)
@Entity
public class FoodInHouse extends AuditModel{
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private long id;

    List<FoodInStorage> foodInStorage;

}
