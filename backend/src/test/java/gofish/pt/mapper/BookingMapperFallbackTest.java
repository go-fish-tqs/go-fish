package gofish.pt.mapper;

import gofish.pt.dto.BookingRequestDTO;
import gofish.pt.dto.BookingResponseDTO;
import gofish.pt.entity.Booking;
import gofish.pt.entity.Item;
import gofish.pt.entity.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class BookingMapperFallbackTest {

    BookingMapperFallback mapper = new BookingMapperFallback();

    @Test
    void toEntity_setsDatesAndPending() {
        BookingRequestDTO dto = new BookingRequestDTO();
        dto.setStartDate(LocalDateTime.now());
        dto.setEndDate(LocalDateTime.now().plusDays(2));

        Booking b = mapper.toEntity(dto);
        assertThat(b).isNotNull();
        assertThat(b.getStartDate()).isEqualTo(dto.getStartDate());
        assertThat(b.getEndDate()).isEqualTo(dto.getEndDate());
        assertThat(b.getStatus()).isNotNull();
    }

    @Test
    void toDTO_mapsItemUserAndPrice() {
        Booking b = new Booking();
        Item item = new Item();
        item.setId(12L);
        item.setName("Rod");
        item.setPhotoUrls(Collections.singletonList("u.jpg"));
        item.setPrice(10.0);
        User u = new User();
        u.setId(3L);
        u.setUsername("john");
        b.setItem(item);
        b.setUser(u);
        b.setStartDate(LocalDateTime.now());
        b.setEndDate(LocalDateTime.now().plusDays(3));

        BookingResponseDTO dto = mapper.toDTO(b);
        assertThat(dto.getItemId()).isEqualTo(12L);
        assertThat(dto.getItemName()).isEqualTo("Rod");
        assertThat(dto.getItemPhotoUrl()).isEqualTo("u.jpg");
        assertThat(dto.getUserId()).isEqualTo(3L);
        assertThat(dto.getUserName()).isEqualTo("john");
        assertThat(dto.getPrice()).isGreaterThan(0.0);
    }
}
