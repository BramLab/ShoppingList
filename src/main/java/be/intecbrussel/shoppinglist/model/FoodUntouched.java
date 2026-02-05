package be.intecbrussel.shoppinglist.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data //Combines @Getter, @Setter, @ToString, @EqualsAndHashCode, @RequiredArgsConstructor
@Entity
public class FoodUntouched extends Food {

    private LocalDate bestBeforeEnd;// = UseBy if perishable soon.
    private double quantityPerPackage;

    @ManyToOne(fetch = FetchType.LAZY)
    private FoodStorage foodStorage;

    // https://www.baeldung.com/lombok-builder-inheritance#lombok-builder-and-inheritance-3
    @Builder(builderMethodName = "foodUntouchedBuilder")
    public FoodUntouched(long id, String name, QuantityUnit typicalUnit, int howMany, String remarks
            , LocalDate bestBeforeEnd, double quantityPerPackage, FoodStorage foodStorage) {
        super(id, name, typicalUnit, howMany, remarks);
        this.bestBeforeEnd = bestBeforeEnd;
        this.quantityPerPackage = quantityPerPackage;
        this.foodStorage = foodStorage;
    }
}
