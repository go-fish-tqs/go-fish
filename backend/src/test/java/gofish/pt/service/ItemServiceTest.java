package gofish.pt.service;

import gofish.pt.entity.Category;
import gofish.pt.entity.Item;
import gofish.pt.entity.Material;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.jpa.domain.Specification;
import gofish.pt.repository.ItemRepository;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import static gofish.pt.repository.ItemSpecifications.nameContains;

class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemService itemService;

    Item i1;
    Item i2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        i1 = new Item(1L, "simple rod", "very simple", List.of(), Material.BRASS, Category.RODS, 5.0);
        i2 = new Item(2L, "cool rod", "very cool", List.of(), Material.GRAPHITE, Category.RODS, 5.0);
    }

    @Test
    void repositoryStartsEmpty() {
        assertThat(itemService.findAll()).isEmpty();
    }

    @Test
    void findById(){
        when(itemRepository.findById(1L)).thenReturn(Optional.of(i1));
        when(itemRepository.findById(2L)).thenReturn(Optional.of(i2));
        when(itemRepository.findById(3L)).thenReturn(Optional.empty());

        assertThat(itemService.findById(1L).isPresent()).isTrue();
        assertThat(itemService.findById(2L).get()).isSameAs(i2);
        assertThat(itemService.findById(3L).isPresent()).isFalse();
    }

    @Test
    void findAll() {
        when(itemRepository.findAll()).thenReturn(List.of(i1, i2));
        assertThat(itemService.findAll()).hasSize(2);
    }

    @Test
    void save() {
        when(itemRepository.save(i1)).thenReturn(i1);
        assertThat(itemService.save(i1)).isSameAs(i1);
        assertThat(itemService.save(null)).isNull();
    }

    @Test
    void delete() {
        itemService.delete(null);
        verify(itemRepository, never()).delete((Item) any());

        itemService.delete(i1);
        verify(itemRepository, times(1)).delete(i1);
    }

    @Test
    void exists() {
        when(itemRepository.existsById(1L)).thenReturn(true);
        when(itemRepository.existsById(3L)).thenReturn(false);

        assertThat(itemService.exists(1L)).isTrue();
        assertThat(itemService.exists(3L)).isFalse();
    }

    @Test
    void findAllSpec() {
        // given
        Specification<Item> spec = nameContains("rod");

        List<Item> mockedResult = List.of(
                new Item(1L, "Rod of Power", "", List.of(), null, null, 10.0),
                new Item(2L, "Fishing Rod", "", List.of(),  null, null, 5.0)
        );

        when(itemRepository.findAll(spec)).thenReturn(mockedResult);

        // when
        List<Item> result = itemService.findAll(spec);

        // then
        // AssertJ: ensure the result matches mocked data
        assertThat(result)
                .isNotEmpty()
                .hasSize(2)
                .extracting(Item::getName)
                .containsExactly("Rod of Power", "Fishing Rod");

        // Mockito: ensure repository was called correctly
        verify(itemRepository).findAll(spec);
    }

}