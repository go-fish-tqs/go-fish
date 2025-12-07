package gofish.pt.boundary;

import gofish.pt.dto.BookingRequestDTO;
import gofish.pt.dto.BookingResponseDTO;
import gofish.pt.dto.BookingStatusDTO;
import gofish.pt.entity.Booking;
import gofish.pt.mapper.BookingMapper;
import gofish.pt.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final BookingMapper bookingMapper;

    @PostMapping
    public ResponseEntity<BookingResponseDTO> createBooking(@Valid @RequestBody BookingRequestDTO request) {

        // 1. Chamas o serviço passando os dados do DTO
        Booking booking = bookingService.createBooking(
                request.getUserId(),
                request.getItemId(),
                request.getStartDate(),
                request.getEndDate()
        );

        // 2. Convertes o resultado para DTO de resposta
        BookingResponseDTO response = bookingMapper.toDTO(booking);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponseDTO> getBooking(@PathVariable Long id) {
        Booking booking = bookingService.getBooking(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reserva nã encontrada!"));
        return ResponseEntity.ok(bookingMapper.toDTO(booking));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<BookingResponseDTO> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody BookingStatusDTO statusDto) {

        Booking booking = bookingService.updateBookingStatus(
                id,
                statusDto.getStatus(),
                statusDto.getOwnerId()
        );

        return ResponseEntity.ok(bookingMapper.toDTO(booking));
    }
}