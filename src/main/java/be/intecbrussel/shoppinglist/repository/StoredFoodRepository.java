package be.intecbrussel.shoppinglist.repository;

import be.intecbrussel.shoppinglist.model.StorageType;
import be.intecbrussel.shoppinglist.model.StoredFood;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoredFoodRepository extends JpaRepository<StoredFood, Long> {
}
