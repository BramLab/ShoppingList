package be.intecbrussel.shoppinglist.repository;

import be.intecbrussel.shoppinglist.model.StoredFood;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoredFoodRepository extends JpaRepository<StoredFood, Long> {

    /**
     * Returns every StoredFood row that belongs to the given home.
     * Replaces the previous in-memory filter in StoredFoodServiceImpl#findAllByHomeId.
     */
    List<StoredFood> findAllByHomeId(long homeId);
}
