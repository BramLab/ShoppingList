package be.intecbrussel.shoppinglist.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
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
// FoodTouched also includes inbetween (e.g. roux) or finished product (green curry).
public class FoodTouched extends Food {

    private LocalDate useBy; // UseBy = perishable soon (sooner than unopened).
    private double amountLeft; // If opened also estimate amount left.
    @ManyToOne(fetch = FetchType.LAZY)
    private Storage storage;
}
