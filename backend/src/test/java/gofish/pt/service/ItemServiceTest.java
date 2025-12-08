package gofish.pt.service;

import gofish.pt.dto.ItemFilter;
import gofish.pt.entity.Category;
import gofish.pt.entity.Item;
import gofish.pt.entity.Material;
import gofish.pt.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ItemServiceTest {

    Item i1;
    Item i2;
    private ItemRepository itemRepository;
    private ItemService itemService;

    @BeforeEach
    void setUp() {
        itemRepository = mock(ItemRepository.class);
        itemService = new ItemService(itemRepository);
        i1 = new Item(1L, "simple rod", "very simple", List.of(), Material.BRASS, Category.RODS, 5.0);
        i2 = new Item(2L, "cool rod", "very cool", List.of(), Material.GRAPHITE, Category.RODS, 7.0);
    }

    @Test
    void repositoryStartsEmpty() {
        assertThat(itemService.findAll()).isEmpty();
    }

    @Test
    void findById() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(i1));
        when(itemRepository.findById(2L)).thenReturn(Optional.of(i2));
        when(itemRepository.findById(3L)).thenReturn(Optional.empty());

        assertThat(itemService.findById(1L)).isPresent();
        assertThat(itemService.findById(2L)).containsSame(i2);
        assertThat(itemService.findById(3L)).isNotPresent();
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
    void findAllSpecWithDefaultSort() {
        // given
        List<Item> mockedResult = List.of(i1, i2);

        when(itemRepository.findAll(any(Specification.class), eq(Sort.by(Sort.Direction.ASC, "id"))))
                .thenReturn(mockedResult);

        // when
        ItemFilter filter = new ItemFilter("rod", null, null, null, null, null, null);
        List<Item> result = itemService.findAll(filter);

        // then
        assertThat(result).isNotEmpty().hasSize(2).extracting(Item::getName).containsExactly(i1.getName(),
                i2.getName());

        verify(itemRepository).findAll(any(Specification.class), eq(Sort.by(Sort.Direction.ASC, "id")));
    }

    @Test
    void findAllWithSort() {
        List<Item> mockedResult = List.of(i1, i2);
        ItemFilter filter = new ItemFilter(null, null, null, null, null, "price", Sort.Direction.DESC);

        when(itemRepository.findAll(any(Specification.class), eq(Sort.by(Sort.Direction.DESC, "price"))))
                .thenReturn(mockedResult);

        List<Item> result = itemService.findAll(filter);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getPrice()).isEqualTo(i1.getPrice());

    }

}