package gofish.pt.repository;

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
        rod = new Item(
                null, // <--- Mete NULL aqui direto! O Hibernate gera o ID.
                "Fishing Rod",
                "Strong rod",
                List.of("img1"),
                Category.RODS,
                Material.CARBON_FIBER,
                19.99,
                true,
                true, // active (admin status)
                null, // deactivationReason
                zePescador, // <--- Mete aqui o USER que criaste!
                null, // bookings (pode ser null se a lista for opcional no construtor)
                null // reviews
        );

        reel = new Item(
                null, // <--- Mete NULL aqui também
                "Fishing Reel",
                "Smooth reel",
                List.of("img2"),
                Category.REELS,
                Material.ALUMINUM,
                7.99,
                true,
                true, // active (admin status)
                null, // deactivationReason
                zePescador, // <--- O mesmo dono
                null, // bookings
                null // reviews
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
    void findAllWhenNull() {
        spec = null;
        assertThat(itemRepository.findAll(spec)).isEqualTo(itemRepository.findAll());
    }

}