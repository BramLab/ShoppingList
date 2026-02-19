package be.intecbrussel.shoppinglist.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.*;

import java.time.LocalDate;


// DECORATOR PATTERN? - FoodTouched around FoodUntouched? -> Keep original name, know original quantity, bestBeforeEnd.
// https://en.wikipedia.org/wiki/Decorator_pattern
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data //Combines @Getter, @Setter, @ToString, @EqualsAndHashCode, @RequiredArgsConstructor
@Entity
// FoodTouched also includes inbetween (e.g. roux) or finished product (green curry).
public class FoodTouched extends Food {

    private LocalDate useBy; // UseBy = perishable soon (sooner than unopened).
    private double amountLeft; // If opened also estimate amount left.

    @Builder(builderMethodName = "foodTouchedBuilder")
    public FoodTouched(long id, String name, String remarks
            , LocalDate useBy, double amountLeft) {

        super(id, name, remarks);
        this.useBy = useBy;
        this.amountLeft = amountLeft;
    }
}
