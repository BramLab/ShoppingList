package be.intecbrussel.shoppinglist.model;

import jakarta.persistence.Entity;
import lombok.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data //Combines @Getter, @Setter, @ToString, @EqualsAndHashCode, @RequiredArgsConstructor
@ToString(callSuper=true)
@Entity
public class FoodOriginal extends Food{

    private LocalDate bestBeforeEnd;// = UseBy if perishable soon.
    private double ml_g_inPackage;

    private LocalDate useBy; // UseBy = perishable soon (sooner than unopened).
    private double ml_g_Left; // If opened also estimate amount left.

    @Builder(builderMethodName = "foodUntouchedBuilder")
    public FoodOriginal(long id, String name, String remarks
            , LocalDate bestBeforeEnd, double ml_g_inPackage
            , LocalDate useBy, double ml_g_Left) {

        super(id, name, remarks);

        this.bestBeforeEnd = bestBeforeEnd;
        this.ml_g_inPackage = ml_g_inPackage;

        this.ml_g_Left = ml_g_Left;
        this.useBy = useBy;
    }

    /*
    // gets the date before
    dates.stream().min(Comparator.<LocalDate>comparingLong(
    x -> Math.abs(ChronoUnit.DAYS.between(x, milestoneDate))
    ).thenComparing(Comparator.naturalOrder()));
    */
    public LocalDate getUseBy(){
        if (useBy != null)  { return useBy; }
        else                { return bestBeforeEnd; }
    }

    public double get_ml_g_Left(){
        if (useBy != null)  { return useBy; }
        else                { return bestBeforeEnd; }
    }
}
