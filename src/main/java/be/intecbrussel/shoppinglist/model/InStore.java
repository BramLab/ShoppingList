package be.intecbrussel.shoppinglist.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data //Combines @Getter, @Setter, @ToString, @EqualsAndHashCode, @RequiredArgsConstructor
@Entity
public class InStore extends AuditModel {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    private Foodproduct foodproduct;

    private LocalDate bestBeforeEnd;
    private double quantityPerPackage;//kg or L
    private int unOpenedPackages;

    private LocalDate useBy;//UseBy = perishable soon, here ESPECIALLY when opened -> also estimate amount left.
    private double amountLeft;//kg or L

    //fresh
    public InStore(Foodproduct foodproduct, LocalDate bestBeforeEnd, double quantityPerPackage, int unOpenedPackages) {
        this.foodproduct = foodproduct;
        this.bestBeforeEnd = bestBeforeEnd;
        this.quantityPerPackage = quantityPerPackage;
        this.unOpenedPackages = unOpenedPackages;
    }

    //useSomething
    public InStore(Foodproduct foodproduct, LocalDate useBy, double amountLeft) {
        this.foodproduct = foodproduct;
        this.useBy = useBy;
        this.amountLeft = amountLeft;
    }

}
