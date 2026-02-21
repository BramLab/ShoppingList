package be.intecbrussel.shoppinglist.repository;

import be.intecbrussel.shoppinglist.model.FoodOriginal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FoodOriginalRepository extends JpaRepository<FoodOriginal, Long> {
}
