package be.intecbrussel.shoppinglist.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;


@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data //Combines @Getter, @Setter, @ToString, @EqualsAndHashCode, @RequiredArgsConstructor
@ToString(callSuper=true)
@Entity
public class FoodUntouched extends Food {
    private LocalDate bestBeforeEnd;// = UseBy if perishable soon.
    private double quantityInPackage;
    @ManyToOne(fetch = FetchType.EAGER)
    private Storage storage;

    // https://www.baeldung.com/lombok-builder-inheritance#lombok-builder-and-inheritance-3
    @Builder(builderMethodName = "foodUntouchedBuilder")
    public FoodUntouched(long id, String name, String remarks
            , LocalDate bestBeforeEnd, double quantityPerPackage, Storage storage) {

        super(id, name, remarks);

        this.bestBeforeEnd = bestBeforeEnd;
        this.quantityInPackage = quantityPerPackage;
        this.storage = storage;
    }

}
