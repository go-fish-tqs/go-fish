package gofish.pt.boundary;

import gofish.pt.dto.BookingRequestDTO;
import gofish.pt.dto.BookingResponseDTO;
import gofish.pt.dto.BookingStatusDTO;
import gofish.pt.entity.Booking;
import gofish.pt.mapper.BookingMapper;
import gofish.pt.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final BookingMapper bookingMapper;

    @PostMapping
    public ResponseEntity<BookingResponseDTO> createBooking(@Valid @RequestBody BookingRequestDTO request) {

        try{
            // 1. Chamas o serviço passando os dados do DTO
            Booking booking = bookingService.createBooking(
                    request.getUserId(),
                    request.getItemId(),
                    request.getStartDate(),
                    request.getEndDate());

            // 2. Convertes o resultado para DTO de resposta
            BookingResponseDTO response = bookingMapper.toDTO(booking);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }catch (IllegalArgumentException e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
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
                statusDto.getOwnerId());

        return ResponseEntity.ok(bookingMapper.toDTO(booking));
    }

    @GetMapping("/item/{itemId}/month")
    public ResponseEntity<List<BookingResponseDTO>> getBookingsByMonth(
            @PathVariable Long itemId,
            @RequestParam int year,
            @RequestParam int month) {
        List<Booking> bookings = bookingService.getBookingsByItemAndMonth(itemId, year, month);
        List<BookingResponseDTO> response = bookings.stream()
                .map(bookingMapper::toDTO)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/item/{itemId}/week")
    public ResponseEntity<List<BookingResponseDTO>> getBookingsByWeek(
            @PathVariable Long itemId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start) {
        List<Booking> bookings = bookingService.getBookingsByItemAndWeek(itemId, start);
        List<BookingResponseDTO> response = bookings.stream()
                .map(bookingMapper::toDTO)
                .toList();
        return ResponseEntity.ok(response);
    }
}