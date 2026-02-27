package be.intecbrussel.shoppinglist.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data //Combines @Getter, @Setter, @ToString, @EqualsAndHashCode, @RequiredArgsConstructor
@Entity
public class AppUser extends AuditModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String username;
    private String email;

    // https://stackoverflow.com/questions/67825729/using-enums-in-a-spring-entity
    @Enumerated(EnumType.STRING)
    private UserRole userRole;

    private String passwordHashed;

    @ManyToOne (optional = false) // MUST be associated
    private Home home;

}