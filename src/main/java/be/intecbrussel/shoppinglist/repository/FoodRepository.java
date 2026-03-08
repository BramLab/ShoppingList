package be.intecbrussel.shoppinglist.repository;

import be.intecbrussel.shoppinglist.model.Food;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FoodRepository extends JpaRepository<Food, Long> {
    Optional<Food> findByName(String name);

    //already by JPA: Optional<Food> findById(long id);
}
