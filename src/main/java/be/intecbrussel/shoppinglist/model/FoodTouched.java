package be.intecbrussel.shoppinglist.model;

import jakarta.persistence.Entity;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// DECORATOR PATTERN? - FoodTouched around FoodUntouched? -> Keep original name, know original quantity, bestBeforeEnd.
// https://en.wikipedia.org/wiki/Decorator_pattern
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Data //Combines @Getter, @Setter, @ToString, @EqualsAndHashCode, @RequiredArgsConstructor
@ToString(callSuper=true)
@Entity
// FoodTouched also includes inbetween (e.g. roux) or finished product (green curry).
public class FoodTouched extends Food {

    private LocalDate useBy; // UseBy = perishable soon (sooner than unopened).
    private double ml_g_Left; // If opened also estimate amount left.
    List<FoodIngredient> foodIngredients; //list of FoodUntouched & FoodTouched (Prepped/Substitutes).

    @Builder(builderMethodName = "foodTouchedBuilder")
    public FoodTouched(long id, String name, String remarks
            , LocalDate useBy, double ml_g_Left, List<FoodIngredient> foodIngredients) {

        super(id, name, remarks);
        this.useBy = useBy;
        this.ml_g_Left = ml_g_Left;
        this.foodIngredients = foodIngredients;
    }
}
