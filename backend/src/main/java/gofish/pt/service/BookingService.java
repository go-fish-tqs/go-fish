package gofish.pt.service;

import gofish.pt.entity.Booking;
import gofish.pt.entity.BookingStatus;
import gofish.pt.repository.BookingRepository;
import gofish.pt.repository.ItemRepository;
import gofish.pt.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    // GET methods

    public Optional<Booking> getBooking(Long id) {
        return bookingRepository.findById(id);
    }

    public List<Booking> getBookings() {
        return bookingRepository.findAll();
    }

    public List<Booking> getBookingsByUserId(Long userId) {
        return bookingRepository.findAllByUserId(userId);
    }

    public List<Booking> getBookingsByItemId(Long itemId) {
        return bookingRepository.findAllByItemId(itemId);
    }

    public List<Booking> getBookingsByItemAndMonth(Long itemId, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();
        return bookingRepository.findByItemIdAndDateRange(itemId, start, end);
    }

    public List<Booking> getBookingsByItemAndWeek(Long itemId, LocalDate weekStart) {
        LocalDate end = weekStart.plusDays(6);
        return bookingRepository.findByItemIdAndDateRange(itemId, weekStart, end);
    }

    // POST method

    public Booking createBooking(Long userId, Long itemId, LocalDate startDate, LocalDate endDate) throws IllegalArgumentException {
        // Lógica para criar uma reserva (omiti detalhes para focar na disponibilidade)
        // 1. Validar utilizador e item
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilizador não encontrado"));
        var item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item não encontrado"));

        // 2. Validar que o utilizador não está a tentar alugar o seu próprio item
        if (item.getOwner().getId().equals(userId)) {
            throw new IllegalArgumentException("Não podes alugar a tua própria cana de pesca!");
        }

        // 3. Validar disponibilidade
        boolean isAvailable = !bookingRepository.existsOverlappingBooking(itemId, startDate, endDate);
        if (!isAvailable) {
            throw new IllegalStateException("Item não disponível nas datas selecionadas");
        }

        // 4. Criar e guardar a reserva
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setItem(item);
        booking.setStartDate(startDate);
        booking.setEndDate(endDate);
        booking.setStatus(BookingStatus.PENDING); // Exemplo de estado inicial

        return bookingRepository.save(booking);
    }

    // PUT method

    public Booking updateBookingStatus(Long bookingId, BookingStatus newStatus, Long ownerId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Reserva nã encontrada!"));

        // SEGURANÇA: Só o dono do item é que pode mexer nisto!
        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Eh lá! Tás a tentar mexer no negócio dos outros?");
        }

        // Regra: Só podes mexer se ainda estiver PENDENTE
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Esta reserva já foi tratada, nã podes mudar mais!");
        }

        // Regra: O dono só pode passar para CONFIRMED ou CANCELLED
        if (newStatus != BookingStatus.CONFIRMED && newStatus != BookingStatus.CANCELLED) { // ou CANCELLED
            throw new IllegalArgumentException("Só podes Confirmar ou Rejeitar!");
        }

        booking.setStatus(newStatus);
        return bookingRepository.save(booking);
    }

    public List<LocalDate> getUnavailableDates(Long itemId, LocalDate start, LocalDate end) {
        // 1. Validar a entrada
        validateDateRange(start, end);

        // 2. Ir à pesca das reservas
        List<Booking> existingBookings = getConflictingBookings(itemId, start, end);

        // 3. Calcular os dias queimados
        return calculateUnavailableDates(start, end, existingBookings);
    }

    private void validateDateRange(LocalDate start, LocalDate end) {
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Ó moce, o início nã pode ser depois do fim!");
        }
    }

    private List<Booking> getConflictingBookings(Long itemId, LocalDate start, LocalDate end) {
        return bookingRepository.findBookingsInRange(itemId, start, end);
    }

    private List<LocalDate> calculateUnavailableDates(LocalDate startDate, LocalDate endDate, List<Booking> bookings) {
        return startDate.datesUntil(endDate.plusDays(1)) // plusDays(1) porque o método é exclusivo no fim
                .filter(date -> isDateUnavailable(date, bookings))
                .sorted()
                .toList();
    }

    private boolean isDateUnavailable(LocalDate date, List<Booking> bookings) {
        return isPastDate(date) || isDateBooked(date, bookings);
    }

    private boolean isPastDate(LocalDate date) {
        return date.isBefore(LocalDate.now());
    }

    private boolean isDateBooked(LocalDate date, List<Booking> bookings) {
        return bookings.stream().anyMatch(booking -> isDateInBookingRange(date, booking));
    }

    private boolean isDateInBookingRange(LocalDate date, Booking booking) {
        LocalDate bStart = booking.getStartDate();
        LocalDate bEnd = booking.getEndDate();

        // Lógica: !(date < start) && !(date > end) -> está no meio ou nas pontas
        return !date.isBefore(bStart) && !date.isAfter(bEnd);
    }

}