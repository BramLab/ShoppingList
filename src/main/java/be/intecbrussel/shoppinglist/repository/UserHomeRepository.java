package be.intecbrussel.shoppinglist.repository;

import be.intecbrussel.shoppinglist.model.UserHome;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserHomeRepository extends JpaRepository<UserHome, Long> {
}
