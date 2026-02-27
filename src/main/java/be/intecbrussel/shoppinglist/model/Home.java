package be.intecbrussel.shoppinglist.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.stereotype.Component;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper=true)
@Data //Combines @Getter, @Setter, @ToString, @EqualsAndHashCode, @RequiredArgsConstructor
@Entity
//@Component //Why does just this class need @Component?
public class Home extends AuditModel{
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private long id;

    private String name;//e.g. "home of Bill & Melinda Gates"

//    @OneToMany(mappedBy = "users")
//    List<AppUser> users;

//    @OneToMany
//    List<StoredFood> storedFood;

    // Maybe this class belongs in service?
    // Actually this app is meant for private use. Users of the same family might interchange data, but data never
    // leaves a family/home (except if they have several homes). => SOLVED added list of Users.

}
