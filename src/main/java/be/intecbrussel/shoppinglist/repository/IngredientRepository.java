package be.intecbrussel.shoppinglist.repository;

import be.intecbrussel.shoppinglist.model.Food;
import be.intecbrussel.shoppinglist.model.FoodIngredient;
import be.intecbrussel.shoppinglist.model.FoodUntouched;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface IngredientRepository extends JpaRepository<FoodIngredient, Long> {
    Optional<Food> findByName(String name);

    Optional<Food> findById(long id);
}
