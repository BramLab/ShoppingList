package be.intecbrussel.shoppinglist.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
public class Food extends AuditModel {
	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	private long id;
    private String name;
    private QuantityUnit typicalUnit;// e.g. flour: g, milk: l, orange: qty, cumin: kl (koffielepel).
    private String remarks;
}
