package be.intecbrussel.shoppinglist.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data //Combines @Getter, @Setter, @ToString, @EqualsAndHashCode, @RequiredArgsConstructor
@Entity
public class FoodSubstitute extends Food {
    private long FoodOriginal;
    private long FoodAlternative;

    @ManyToOne(fetch = FetchType.LAZY)
    private Recipe recipe;

    private String aspect;
}
