package be.intecbrussel.shoppinglist.repository;

import be.intecbrussel.shoppinglist.model.FoodAtHome;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FoodAtHomeRepository extends JpaRepository<FoodAtHome, Long> {
}
