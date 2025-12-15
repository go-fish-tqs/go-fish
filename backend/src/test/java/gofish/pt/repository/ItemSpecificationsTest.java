package gofish.pt.repository;

import app.getxray.xray.junit.customjunitxml.annotations.Requirement;
import gofish.pt.entity.Category;
import gofish.pt.entity.Item;
import gofish.pt.entity.Material;
import gofish.pt.entity.User;
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

    @Autowired
    private UserRepository userRepository; // <--- 1. PRECISAS DISTO AQUI

    private Item rod;
    private Item reel;

    @BeforeEach
    void setup() {
        itemRepository.deleteAll();
        userRepository.deleteAll(); // Limpa a casa toda

        // 2. CRIA UM DONO (USER) PRIMEIRO
        User zePescador = new User();
        zePescador.setUsername("Zé do Pipo");
        zePescador.setEmail("ze@peixe.pt");
        zePescador.setPassword("segredo123");
        zePescador.setLocation("Faro");
        // Preenche os campos obrigatórios do User...

        userRepository.save(zePescador); // <--- O user ganha ID aqui

        // 3. AGORA CRIA OS ITENS ASSOCIADOS AO ZÉ
        rod = new Item();
        rod.setName("Fishing Rod");
        rod.setDescription("Strong rod");
        rod.setPhotoUrls(List.of("img1"));
        rod.setCategory(Category.RODS);
        rod.setMaterial(Material.CARBON_FIBER);
        rod.setPrice(15.99);
        rod.setAvailable(true);
        rod.setOwner(zePescador); // <--- Associa o dono que acab

        reel = new Item();
        reel.setName("Fishing Reel");
        reel.setDescription("Smooth reel");
        reel.setPhotoUrls(List.of("img2"));
        reel.setCategory(Category.REELS);
        reel.setMaterial(Material.ALUMINUM);
        reel.setPrice(7.99);
        reel.setAvailable(true);
        reel.setOwner(zePescador); // <--- Associa o dono que acab

        itemRepository.saveAll(List.of(rod, reel));
    }

    @Test
    @Requirement("GF-45")
    void filterByCategory() {
        spec = categoryIs(Category.RODS);

        result = itemRepository.findAll(spec);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategory()).isEqualTo(Category.RODS);
    }

    @Test
    @Requirement("GF-44")
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
    @Requirement("GF-45")
    void filterByMaterial() {
        spec = materialIs(Material.CARBON_FIBER);
        result = itemRepository.findAll(spec);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMaterial()).isEqualTo(Material.CARBON_FIBER);
    }

    @Test
    @Requirement("GF-45")
    void filterByMultipleConditions() {
        spec = Specification.allOf(nameContains("fishing"), priceBetween(10d, 20d), availableIs(true));
        result = itemRepository.findAll(spec);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Fishing Rod");
    }

    @Test
    @Requirement("GF-42")
    void sortByPrice() {
        spec = null;
        sort = Sort.by(Sort.Direction.DESC, "price");
        result = itemRepository.findAll(spec, sort);
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getPrice()).isGreaterThan(result.get(1).getPrice());
    }

    @Test
    @Requirement("GF-42")
    void findAllWhenNull() {
        spec = null;
        assertThat(itemRepository.findAll(spec)).isEqualTo(itemRepository.findAll());
    }

}