package be.intecbrussel.shoppinglist.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.SoftDeleteType;

@Inheritance(strategy = InheritanceType.JOINED)//otherways we have a massive table with 17 or so columns.
@DiscriminatorColumn(name = "dtype")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data //Combines @Getter, @Setter, @ToString, @EqualsAndHashCode, @RequiredArgsConstructor
@ToString(callSuper=true)
@SoftDelete(columnName = "deleted_at", strategy = SoftDeleteType.TIMESTAMP)
@Entity
public class Food extends AuditModel {
	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	private long id;
    private String name;
    private String remarks;

//	@ManyToOne
//	private StoredFood storedFood;
}
