package gofish.pt.service;

import app.getxray.xray.junit.customjunitxml.annotations.Requirement;
import gofish.pt.dto.ItemDTO;
import gofish.pt.dto.ItemFilter;
import gofish.pt.entity.Category;
import gofish.pt.entity.Item;
import gofish.pt.entity.Material;
import gofish.pt.mapper.ItemMapper;
import gofish.pt.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ItemServiceTest {

    Item i1;
    Item i2;
    ItemDTO dto1;
    ItemDTO dto2;

    @Mock
    ItemRepository repository;

    @Mock
    ItemMapper itemMapper;

    @InjectMocks
    ItemService itemService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        i1 = new Item(1L, "simple rod", "very simple", List.of(), Category.RODS, Material.BRASS, 5.0, true, null, null,
                null);
        i2 = new Item(2L, "cool rod", "very cool", List.of(), Category.RODS, Material.GRAPHITE, 7.0, true, null, null,
                null);
        dto1 = new ItemDTO("simple rod", "very simple", List.of(), Category.RODS, Material.BRASS, 5.0, 1L);
        dto2 = new ItemDTO("cool rod", "very cool", List.of(), Category.RODS, Material.GRAPHITE, 7.0, 1L);
    }

    @Test
    @Requirement("GF-42")
    void repositoryStartsEmpty() {
        assertThat(itemService.findAll()).isEmpty();
    }

    @Test
    @Requirement("GF-46")
    void findById() {
        when(repository.findById(1L)).thenReturn(Optional.of(i1));
        when(repository.findById(2L)).thenReturn(Optional.of(i2));
        when(repository.findById(3L)).thenReturn(Optional.empty());

        assertThat(itemService.findById(1L)).isPresent();
        assertThat(itemService.findById(2L)).containsSame(i2);
        assertThat(itemService.findById(3L)).isNotPresent();
    }

    @Test
    @Requirement("GF-42")
    void findAll() {
        when(repository.findAll()).thenReturn(List.of(i1, i2));
        assertThat(itemService.findAll()).hasSize(2);
    }

    @Test
    @Requirement("GF-57")
    void save() {
        when(itemMapper.toEntity(dto1)).thenReturn(i1);
        when(repository.save(i1)).thenReturn(i1);

        assertThat(itemService.save(dto1)).isSameAs(i1);
    }

    @Test
    @Requirement("GF-57")
    void saveReturnsNullWhenMapperReturnsNull() {
        when(itemMapper.toEntity(any(ItemDTO.class))).thenReturn(null);

        assertThat(itemService.save(dto1)).isNull();
        verify(repository, never()).save(any());
    }

    @Test
    void delete() {
        itemService.delete(null);
        verify(repository, never()).delete((Item) any());

        itemService.delete(i1);
        verify(repository, times(1)).delete(i1);
    }

    @Test
    void exists() {
        when(repository.existsById(1L)).thenReturn(true);
        when(repository.existsById(3L)).thenReturn(false);

        assertThat(itemService.exists(1L)).isTrue();
        assertThat(itemService.exists(3L)).isFalse();
    }

    @Test
    @Requirement("GF-42")
    void findAllSpecWithDefaultSort() {
        List<Item> mockedResult = List.of(i1, i2);

        when(repository.findAll(any(Specification.class), eq(Sort.by(Sort.Direction.ASC, "id"))))
                .thenReturn(mockedResult);

        ItemFilter filter = new ItemFilter("rod", null, null, null, null, null, null);
        List<Item> result = itemService.findAll(filter);

        // then
        assertThat(result).isNotEmpty().hasSize(2).extracting(Item::getName).containsExactly(i1.getName(),
                i2.getName());

        verify(repository).findAll(any(Specification.class), eq(Sort.by(Sort.Direction.ASC, "id")));
    }

    @Test
    @Requirement("GF-42")
    void findAllWithSort() {
        List<Item> mockedResult = List.of(i1, i2);
        ItemFilter filter = new ItemFilter(null, null, null, null, null, "price", Sort.Direction.DESC);

        when(repository.findAll(any(Specification.class), eq(Sort.by(Sort.Direction.DESC, "price"))))
                .thenReturn(mockedResult);

        List<Item> result = itemService.findAll(filter);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getPrice()).isEqualTo(i1.getPrice());
    }
}
