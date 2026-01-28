package be.intecbrussel.shoppinglist.repository;

import be.intecbrussel.shoppinglist.model.Foodproduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FoodproductRepository extends JpaRepository<Foodproduct, Long> {
}
