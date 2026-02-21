package be.intecbrussel.shoppinglist.model;

import jakarta.persistence.Entity;
import lombok.*;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data //Combines @Getter, @Setter, @ToString, @EqualsAndHashCode, @RequiredArgsConstructor
@ToString(callSuper=true)
@Entity
public class FoodOriginal extends Food{

    private LocalDate bestBeforeEnd;// = UseBy if perishable soon.
    private double original_ml_g;
    private LocalDate useBy; // UseBy = perishable soon (sooner than unopened).
    private double remaining_ml_g; // If opened also estimate amount left.

    @Builder(builderMethodName = "foodOriginalBuilder")
    public FoodOriginal(long id, String name, String remarks
            , LocalDate bestBeforeEnd, double original_ml_g
            , LocalDate useBy, double remaining_ml_g) {

        super(id, name, remarks);

        this.bestBeforeEnd = bestBeforeEnd;
        this.original_ml_g = original_ml_g;

        this.remaining_ml_g = remaining_ml_g;
        this.useBy = useBy;
    }

    // TODO: Using last bit should also softdelete product instead.
    // If ml_g_Left is realy zero because nothing is left, then problem.

    public LocalDate getUseBy(){
        if (useBy != null)  { return useBy; }
        else                { return bestBeforeEnd; }
    }

    public double get_ml_g_Left(){
        if (remaining_ml_g != 0) { return remaining_ml_g; }
        else                { return original_ml_g; }
    }
}
