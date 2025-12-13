package gofish.pt.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

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
    @FutureOrPresent(message = "A data de início tem de ser hoje ou no futuro!")
    private LocalDateTime startDate;

    @NotNull
    @FutureOrPresent(message = "A data de fim tem de ser hoje ou no futuro!")
    private LocalDateTime endDate;
}