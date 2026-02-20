package be.intecbrussel.shoppinglist.repository;

import be.intecbrussel.shoppinglist.model.Food;
import be.intecbrussel.shoppinglist.model.Storage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StorageRepository extends JpaRepository<Storage, Long> {
    Optional<Storage> findByName(String name);

    Optional<Storage> findById(long id);
}
