package be.intecbrussel.shoppinglist.model;

import jakarta.persistence.Entity;
import lombok.*;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
//@RequiredArgsConstructor
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

//    public LocalDate getBestBeforeEnd() {
//        return bestBeforeEnd;
//    }
//
//    public double getOriginal_ml_g() {
//        return original_ml_g;
//    }

    public LocalDate getUseBy(){
        if (useBy != null)  { return useBy; }
        else                { return bestBeforeEnd; }
    }

//    public double getRemaining_ml_g(){
//        return remaining_ml_g;
//    }
//
//    public void setBestBeforeEnd(LocalDate bestBeforeEnd) {
//        this.bestBeforeEnd = bestBeforeEnd;
//    }

    public void setOriginal_ml_g(double original_ml_g) {
        this.original_ml_g = original_ml_g;
        this.remaining_ml_g = original_ml_g;
    }

//    public void setUseBy(LocalDate useBy) {
//        this.useBy = useBy;
//    }

    // TODO: Using last bit should also softdelete product instead.
    // If ml_g_Left is realy zero because nothing is left, then problem.
    public void setRemaining_ml_g(double remaining_ml_g) {
        this.remaining_ml_g = remaining_ml_g;
        if (remaining_ml_g <= 0d) {
            this.remaining_ml_g = -1d;
        }
    }

}
