package gofish.pt.repository;

import gofish.pt.entity.Category;
import gofish.pt.entity.Item;
import gofish.pt.entity.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long>, JpaSpecificationExecutor<Item> {
    Optional<Item> findById(long id);
    List<Item> findAllByNameContainingIgnoreCase(String name);
    List<Item> findAllByCategory(Category category);
    List<Item> findAllByMaterial(Material material);
    List<Item> findAllByPriceBetween(Double min, Double max);
    List<Item> findAllByAvailable(boolean available);
}

