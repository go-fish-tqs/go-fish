package gofish.pt.dto;

import gofish.pt.entity.BookingStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookingStatusDTO {
    @NotNull(message = "Tens de decidir se aprovas ou nã!")
    private BookingStatus status;

    // Num sistema real com login, nã precisavas disto (vinha do Token).
    // Mas como estamos a desenrascar:
    @NotNull(message = "Quem é que está a mandar nisto?")
    private Long ownerId;
}