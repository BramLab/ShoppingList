package be.intecbrussel.shoppinglist.repository;

import be.intecbrussel.shoppinglist.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {

    // findByUsername is a common method naming convention in programming,
    // especially in Java frameworks like Spring Data JPA,
    // for retrieving a user object from a database or data store using their unique username,
    // often automatically generating SQL like SELECT * FROM users WHERE username = ?,
    // simplifying data access by letting developers define methods in repositories
    // instead of writing boilerplate queries.
    Optional<User> findByUsername(String username);

    Optional<Object> findByEmail(String email);

    Optional<User> findById(long instructorId);
}

