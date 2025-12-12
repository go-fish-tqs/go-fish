package gofish.pt.mapper;

import gofish.pt.dto.BookingRequestDTO;
import gofish.pt.dto.BookingResponseDTO;
import gofish.pt.entity.Booking;
import gofish.pt.entity.BookingStatus;
import gofish.pt.entity.Item;
import gofish.pt.entity.User;
import org.springframework.stereotype.Component;

import java.time.temporal.ChronoUnit;

@Component
public class BookingMapperManualImpl {

    public Booking toEntity(BookingRequestDTO dto) {
        if (dto == null) return null;
        Booking b = new Booking();
        b.setStartDate(dto.getStartDate());
        b.setEndDate(dto.getEndDate());
        b.setStatus(BookingStatus.PENDING);
        return b;
    }

    public BookingResponseDTO toDTO(Booking booking) {
        if (booking == null) return null;
        BookingResponseDTO dto = new BookingResponseDTO();
        Item item = booking.getItem();
        User user = booking.getUser();
        if (item != null) {
            dto.setItemId(item.getId());
            dto.setItemName(item.getName());
            dto.setItemPhotoUrl((item.getPhotoUrls() != null && !item.getPhotoUrls().isEmpty()) ? item.getPhotoUrls().get(0) : null);
            dto.setPrice(item.getPrice() != null ? calculatePrice(booking) : 0.0);
        }
        if (user != null) {
            dto.setUserId(user.getId());
            dto.setUserName(user.getUsername());
        }
        dto.setStartDate(booking.getStartDate());
        dto.setEndDate(booking.getEndDate());
        dto.setStatus(booking.getStatus());
        return dto;
    }

    private Double calculatePrice(Booking booking) {
        if (booking.getItem() == null || booking.getStartDate() == null || booking.getEndDate() == null) return 0.0;
        long days = ChronoUnit.DAYS.between(booking.getStartDate(), booking.getEndDate());
        if (days == 0) days = 1;
        Double pricePerDay = booking.getItem().getPrice();
        if (pricePerDay == null) return 0.0;
        return days * pricePerDay;
    }
}
