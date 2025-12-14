package gofish.pt.dto;

import gofish.pt.entity.BookingStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class BookingResponseDTO {

    private Long id;
    private LocalDate startDate;
    private LocalDate endDate;
    private BookingStatus status;
    private Double price; // Vamos calcular isto na hora de converter!

    // Nã mandes o Item todo, manda só o essencial para mostrar no ecrã
    private Long itemId;
    private String itemName;
    private String itemPhotoUrl; // A primeira foto, por exemplo

    private Long userId; // Quem reservou
    private String userName; // Nome de quem reservou
}