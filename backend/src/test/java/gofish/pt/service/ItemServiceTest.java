package gofish.pt.service;

import gofish.pt.dto.BlockDateRequestDTO;
import gofish.pt.dto.ItemDTO;
import gofish.pt.dto.ItemFilter;
import gofish.pt.entity.*;
import gofish.pt.mapper.ItemMapper;
import gofish.pt.repository.BlockedDateRepository;
import gofish.pt.repository.BookingRepository;
import gofish.pt.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;


import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class ItemServiceTest {

    User owner;
    Item i1;
    Item i2;
    ItemDTO dto1;
    ItemDTO dto2;

    @Mock
    ItemRepository itemRepository;

    @Mock
    BlockedDateRepository blockedDateRepository;

    @Mock
    BookingRepository bookingRepository;

    @Mock
    ItemMapper itemMapper;

    @InjectMocks
    ItemService itemService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        owner = new User();
        owner.setId(1L);
        owner.setUsername("owner");
        owner.setEmail("owner@test.com");
        
        i1 = new Item(1L, "simple rod", "very simple", List.of(), Category.RODS, Material.BRASS, 5.0, true, owner, null,
                null);
        i2 = new Item(2L, "cool rod", "very cool", List.of(), Category.RODS, Material.GRAPHITE, 7.0, true, owner, null,
                null);
        dto1 = new ItemDTO("simple rod", "very simple", List.of(), Category.RODS, Material.BRASS, 5.0, 1L);
        dto2 = new ItemDTO("cool rod", "very cool", List.of(), Category.RODS, Material.GRAPHITE, 7.0, 1L);
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
        when(itemMapper.toEntity(dto1)).thenReturn(i1);
        when(itemRepository.save(i1)).thenReturn(i1);

        assertThat(itemService.save(dto1)).isSameAs(i1);
    }

    @Test
    void saveReturnsNullWhenMapperReturnsNull() {
        when(itemMapper.toEntity(any(ItemDTO.class))).thenReturn(null);

        assertThat(itemService.save(dto1)).isNull();
        verify(itemRepository, never()).save(any());
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
        List<Item> mockedResult = List.of(i1, i2);

        when(itemRepository.findAll(any(Specification.class), eq(Sort.by(Sort.Direction.ASC, "id"))))
                .thenReturn(mockedResult);

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

    @Nested
    @DisplayName("Tests for blockDateRange method")
    class BlockDateRangeTests {

        @Test
        void whenValidRequest_thenBlockDate() {
            BlockDateRequestDTO request = new BlockDateRequestDTO();
            request.setStartDate(LocalDate.now());
            request.setEndDate(LocalDate.now().plusDays(5));
            request.setReason("Maintenance");

            when(itemRepository.findById(1L)).thenReturn(Optional.of(i1));
            when(bookingRepository.existsOverlappingBooking(anyLong(), any(), any())).thenReturn(false);
            when(blockedDateRepository.save(any(BlockedDate.class))).thenAnswer(invocation -> invocation.getArgument(0));

            BlockedDate result = itemService.blockDateRange(1L, request, 1L);

            assertThat(result).isNotNull();
            assertThat(result.getReason()).isEqualTo("Maintenance");
            assertThat(result.getItem()).isEqualTo(i1);
            verify(blockedDateRepository, times(1)).save(any(BlockedDate.class));
        }

        @Test
        void whenUserIsNotOwner_thenThrowForbidden() {
            BlockDateRequestDTO request = new BlockDateRequestDTO();
            when(itemRepository.findById(1L)).thenReturn(Optional.of(i1));

            assertThatThrownBy(() -> itemService.blockDateRange(1L, request, 99L)) // Different owner ID
                    .isInstanceOf(ResponseStatusException.class)
                    .hasFieldOrPropertyWithValue("statusCode", HttpStatus.FORBIDDEN);
        }

        @Test
        void whenItemNotFound_thenThrowNotFound() {
            BlockDateRequestDTO request = new BlockDateRequestDTO();
            when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> itemService.blockDateRange(1L, request, 1L))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasFieldOrPropertyWithValue("statusCode", HttpStatus.NOT_FOUND);
        }

        @Test
        void whenBookingConflictExists_thenThrowConflict() {
            BlockDateRequestDTO request = new BlockDateRequestDTO();
            request.setStartDate(LocalDate.now());
            request.setEndDate(LocalDate.now().plusDays(5));

            when(itemRepository.findById(1L)).thenReturn(Optional.of(i1));
            when(bookingRepository.existsOverlappingBooking(anyLong(), any(), any())).thenReturn(true);

            assertThatThrownBy(() -> itemService.blockDateRange(1L, request, 1L))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasFieldOrPropertyWithValue("statusCode", HttpStatus.CONFLICT);
        }
    }

    @Nested
    @DisplayName("Tests for unblockDateRange method")
    class UnblockDateRangeTests {

        @Test
        void whenValidRequest_thenDeleteBlock() {
            BlockedDate blockedDate = new BlockedDate(LocalDate.now(), LocalDate.now().plusDays(1), "Test", i1);
            when(blockedDateRepository.findById(1L)).thenReturn(Optional.of(blockedDate));
            doNothing().when(blockedDateRepository).delete(any(BlockedDate.class));

            itemService.unblockDateRange(1L, 1L);

            verify(blockedDateRepository, times(1)).delete(blockedDate);
        }

        @Test
        void whenUserIsNotOwner_thenThrowForbiddenOnDelete() {
            BlockedDate blockedDate = new BlockedDate(LocalDate.now(), LocalDate.now().plusDays(1), "Test", i1);
            when(blockedDateRepository.findById(1L)).thenReturn(Optional.of(blockedDate));

            assertThatThrownBy(() -> itemService.unblockDateRange(1L, 99L)) // Different owner ID
                    .isInstanceOf(ResponseStatusException.class)
                    .hasFieldOrPropertyWithValue("statusCode", HttpStatus.FORBIDDEN);
        }

        @Test
        void whenBlockedDateNotFound_thenThrowNotFound() {
            when(blockedDateRepository.findById(anyLong())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> itemService.unblockDateRange(1L, 1L))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasFieldOrPropertyWithValue("statusCode", HttpStatus.NOT_FOUND);
        }
    }
}
