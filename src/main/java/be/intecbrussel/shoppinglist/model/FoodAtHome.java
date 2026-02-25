package be.intecbrussel.shoppinglist.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper=true)
@Data //Combines @Getter, @Setter, @ToString, @EqualsAndHashCode, @RequiredArgsConstructor
@Entity
public class FoodAtHome extends AuditModel{
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private long id;

    List<StoredFood> storedFood;

}
