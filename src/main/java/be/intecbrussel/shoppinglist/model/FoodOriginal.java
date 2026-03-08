package be.intecbrussel.shoppinglist.model;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import lombok.*;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
//@RequiredArgsConstructor
@Data //Combines @Getter, @Setter, @ToString, @EqualsAndHashCode, @RequiredArgsConstructor
@ToString(callSuper=true)
@Entity
@PrimaryKeyJoinColumn(name = "food_id")
public class FoodOriginal extends Food {

    private LocalDate bestBeforeEnd;// = UseBy if perishable soon.
    private double original_ml_g;
    private LocalDate useBy; // UseBy = perishable soon (sooner than unopened).
    private double remaining_ml_g; // If opened also estimate ml_g_left left.

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

    public LocalDate getUseByElseBestBeforeEnd(){
        if (useBy != null)  { return useBy; }
        else                { return bestBeforeEnd; }
    }

    public void setOriginal_ml_g(double original_ml_g) {
        this.original_ml_g = original_ml_g;
        this.remaining_ml_g = original_ml_g;
    }

    // TODO: Using last bit should also softdelete product instead.
    // How to make product deleted when last bit is used?
    // If ml_g_Left is realy zero because nothing is left, then problem.
    public void setRemaining_ml_g(double remaining_ml_g) {
        this.remaining_ml_g = remaining_ml_g;
        if (remaining_ml_g <= 0d) {
            this.remaining_ml_g = -1d;
        }
    }

}
