package be.intecbrussel.shoppinglist.repository;

import be.intecbrussel.shoppinglist.model.StorageType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StorageRepository extends JpaRepository<StorageType, Long> {
    Optional<StorageType> findByName(String name);

    Optional<StorageType> findById(long id);
}
