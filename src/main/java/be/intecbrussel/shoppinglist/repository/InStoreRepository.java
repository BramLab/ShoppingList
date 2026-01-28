package be.intecbrussel.shoppinglist.repository;

import be.intecbrussel.shoppinglist.model.InStore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InStoreRepository extends JpaRepository<InStore, Long> {
}
