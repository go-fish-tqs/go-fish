package gofish.pt.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data // O Lombok faz os getters, setters, etc.
public class BookingRequestDTO {

    @NotNull(message = "Tens de dizer qual é o item, moce!")
    private Long itemId;

    // Nota: Num sistema real, o userId vem do Token de Login (SecurityContext),
    // mas se ainda nã tens login, mete aqui para desenrascar.
    @NotNull(message = "Quem é que vai pagar isto?")
    private Long userId;

    @NotNull
    @Future(message = "A data de início tem de ser no futuro!")
    private LocalDate startDate;

    @NotNull
    @Future(message = "A data de fim tem de ser no futuro!")
    private LocalDate endDate;
}