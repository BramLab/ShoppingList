package be.intecbrussel.shoppinglist.repository;

import be.intecbrussel.shoppinglist.model.Food;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FoodRepository extends JpaRepository<Food, Long> {
}
