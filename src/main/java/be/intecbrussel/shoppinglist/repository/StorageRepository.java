package be.intecbrussel.shoppinglist.repository;

import be.intecbrussel.shoppinglist.model.Storage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StorageRepository extends JpaRepository<Storage, Long> {
}
