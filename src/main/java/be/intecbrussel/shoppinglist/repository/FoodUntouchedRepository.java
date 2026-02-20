package be.intecbrussel.shoppinglist.repository;

import be.intecbrussel.shoppinglist.model.FoodUntouched;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FoodUntouchedRepository extends JpaRepository<FoodUntouched, Long> {
}
