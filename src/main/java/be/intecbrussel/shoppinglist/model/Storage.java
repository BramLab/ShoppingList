package be.intecbrussel.shoppinglist.model;

import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import lombok.*;

@Transactional
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Data //Combines @Getter, @Setter, @ToString, @EqualsAndHashCode, @RequiredArgsConstructor
@Entity
@ToString(callSuper=true)
public class Storage {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private long id;
    private String name;
    private String remarks;
}
