package gofish.pt.repository;

import gofish.pt.entity.Category;
import gofish.pt.entity.Item;
import gofish.pt.entity.Material;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static gofish.pt.repository.ItemSpecifications.*;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ItemSpecificationsTest {
    private Specification<Item> spec;
    private List<Item> result;
    private Sort sort;

    @Autowired
    private ItemRepository itemRepository;
    private Item rod;
    private Item reel;

    @BeforeEach
    void setup() {
        itemRepository.deleteAll();

        rod = new Item(
                1L,
                "Fishing Rod",
                "Strong rod",
                List.of("img1"),
                Material.CARBON_FIBER,
                Category.RODS,
                19.99
        );

        reel = new Item(
                2L,
                "Fishing Reel",
                "Smooth reel",
                List.of("img2"),
                Material.ALUMINUM,
                Category.REELS,
                7.99
        );

        itemRepository.saveAll(List.of(rod, reel));
    }

    @Test
    void filterByCategory() {
        spec = categoryIs(Category.RODS);

        result = itemRepository.findAll(spec);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategory()).isEqualTo(Category.RODS);
    }

    @Test
    void filterByName() {
        spec = nameContains("fishing");
        result = itemRepository.findAll(spec);
        assertThat(result).hasSize(2);

        spec = nameContains("rod");
        result = itemRepository.findAll(spec);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Fishing Rod");

    }

    @Test
    void filterByMaterial() {
        spec = materialIs(Material.CARBON_FIBER);
        result = itemRepository.findAll(spec);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMaterial()).isEqualTo(Material.CARBON_FIBER);
    }

    @Test
    void filterByMultipleConditions() {
        spec = Specification.allOf(nameContains("fishing"), priceBetween(10d, 20d), availableIs(true));
        result = itemRepository.findAll(spec);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Fishing Rod");
    }

    @Test
    void sortByPrice() {
        spec = null;
        sort = Sort.by(Sort.Direction.DESC, "price");
        result = itemRepository.findAll(spec, sort);
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getPrice()).isGreaterThan(result.get(1).getPrice());
    }

    @Test
    void findAllWhenNull(){
        spec = null;
        assertThat(itemRepository.findAll(spec)).isEqualTo(itemRepository.findAll());
    }

}