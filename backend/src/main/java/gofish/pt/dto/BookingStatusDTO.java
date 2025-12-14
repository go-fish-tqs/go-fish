package gofish.pt.dto;

import gofish.pt.entity.BookingStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookingStatusDTO {
    @NotNull(message = "Tens de decidir se aprovas ou nã!")
    private BookingStatus status;
}