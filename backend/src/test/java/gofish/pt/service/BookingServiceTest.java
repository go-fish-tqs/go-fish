package gofish.pt.service;

import app.getxray.xray.junit.customjunitxml.annotations.Requirement;
import gofish.pt.entity.*;
import gofish.pt.repository.BlockedDateRepository;
import gofish.pt.repository.BookingRepository;
import gofish.pt.repository.ItemRepository;
import gofish.pt.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // <--- Liga o Mockito
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BlockedDateRepository blockedDateRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private BookingService bookingService;

    // Dados de teste (para não repetir código)
    private User renter;
    private User owner;
    private Item fishingRod;
    private Booking booking;

    @BeforeEach
    void setup() {
        bookingService = new BookingService(bookingRepository, itemRepository, userRepository, blockedDateRepository,
                userService);
        renter = new User();
        renter.setId(10L);
        renter.setUsername("ze_aluga");

        owner = new User();
        owner.setId(20L);
        owner.setUsername("manel_dono");

        fishingRod = new Item();
        fishingRod.setId(5L);
        fishingRod.setName("Cana Boa");
        fishingRod.setOwner(owner); // O item é do Manel
        fishingRod.setActive(true); // Item is active

        booking = new Booking();
        booking.setId(100L);
        booking.setUser(renter);
        booking.setItem(fishingRod);
        booking.setStatus(BookingStatus.PENDING);
        booking.setStartDate(LocalDate.now().plusDays(1));
        booking.setEndDate(LocalDate.now().plusDays(3));
    }

    // --- TESTES DE CRIAR RESERVA (createBooking) ---

    @Test
    @DisplayName("Deve criar reserva com sucesso quando tudo está livre")
    @Requirement("GF-48")
    void shouldCreateBooking_WhenAvailable() {
        // Arrange (Preparar o terreno)
        LocalDate start = LocalDate.now().plusDays(5);
        LocalDate end = LocalDate.now().plusDays(7);

        when(userRepository.findById(renter.getId())).thenReturn(Optional.of(renter));
        when(itemRepository.findById(fishingRod.getId())).thenReturn(Optional.of(fishingRod));
        // User is active (not suspended)
        when(userService.isUserActive(renter.getId())).thenReturn(true);
        // O repositório diz que NÃO há sobreposição (false)
        when(bookingRepository.existsOverlappingBooking(fishingRod.getId(), start, end)).thenReturn(false);
        // Quando salvar, devolve a reserva criada
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act (Ação)
        Booking created = bookingService.createBooking(renter.getId(), fishingRod.getId(), start, end);

        // Assert (Verificar)
        assertThat(created).isNotNull();
        assertThat(created.getStatus()).isEqualTo(BookingStatus.PENDING);
        assertThat(created.getUser()).isEqualTo(renter);

        // Garante que o método save() foi chamado uma vez
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    @DisplayName("Deve lançar erro se o item já estiver ocupado")
    @Requirement("GF-51")
    void shouldThrowError_WhenItemIsOccupied() {
        LocalDate start = LocalDate.now().plusDays(5);
        LocalDate end = LocalDate.now().plusDays(7);

        when(userRepository.findById(renter.getId())).thenReturn(Optional.of(renter));
        when(itemRepository.findById(fishingRod.getId())).thenReturn(Optional.of(fishingRod));
        // User is active (not suspended)
        when(userService.isUserActive(renter.getId())).thenReturn(true);

        // O repositório diz que SIM, há sobreposição (true)
        when(bookingRepository.existsOverlappingBooking(fishingRod.getId(), start, end)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> bookingService.createBooking(renter.getId(), fishingRod.getId(), start, end))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Item não disponível");

        // Garante que NUNCA tentou salvar na base de dados
        verify(bookingRepository, never()).save(any());
    }

    // --- TESTES DE APROVAR/ATUALIZAR (updateBookingStatus) ---

    @Test
    @DisplayName("Dono deve conseguir confirmar uma reserva pendente")
    @Requirement("GF-48")
    void ownerShouldConfirmBooking() {
        // Arrange
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        Booking updated = bookingService.updateBookingStatus(booking.getId(), BookingStatus.CONFIRMED, owner.getId());

        // Assert
        assertThat(updated.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    @DisplayName("Deve lançar erro se quem tenta confirmar não for o dono")
    @Requirement("GF-48")
    void shouldThrowError_WhenNonOwnerTriesToConfirm() {
        // Arrange
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        // Act & Assert (O Zé (renter) tenta confirmar a reserva dele próprio ->
        // PROIBIDO!)
        assertThatThrownBy(
                () -> bookingService.updateBookingStatus(booking.getId(), BookingStatus.CONFIRMED, renter.getId()))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    @DisplayName("Não deve deixar alterar uma reserva que já foi aceite")
    @Requirement("GF-48")
    void shouldThrowError_WhenUpdatingConfirmedBooking() {
        // Arrange
        booking.setStatus(BookingStatus.CONFIRMED); // Já está aceite
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        // Act & Assert
        assertThatThrownBy(
                () -> bookingService.updateBookingStatus(booking.getId(), BookingStatus.CANCELLED, owner.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("já foi tratada");
    }

    // --- TESTES DE DISPONIBILIDADE (checkAvailability) ---

    @Test
    @DisplayName("Deve devolver lista de dias ocupados + dias passados")
    @Requirement("GF-49")
    void shouldReturnUnavailableDates() {
        // Arrange
        LocalDate today = LocalDate.now();
        LocalDate queryStart = today.minusDays(1); // Ontem (Passado)
        LocalDate queryEnd = today.plusDays(5);

        // Simular uma reserva existente para daqui a 2 dias
        Booking existing = new Booking();
        existing.setStartDate(today.plusDays(2));
        existing.setEndDate(today.plusDays(2)); // Reserva de 1 dia
        existing.setStatus(BookingStatus.CONFIRMED);

        when(bookingRepository.findBookingsInRange(eq(fishingRod.getId()), any(), any()))
                .thenReturn(List.of(existing));

        when(blockedDateRepository.findBlockedDatesInRange(eq(fishingRod.getId()), any(), any()))
                .thenReturn(List.of());

        // Act
        List<LocalDate> blockedDates = bookingService.getUnavailableDates(fishingRod.getId(), queryStart, queryEnd);

        // Assert
        // Esperamos:
        // 1. O dia de Ontem (porque é passado)
        // 2. O dia da reserva existente (daqui a 2 dias)
        assertThat(blockedDates).contains(
                today.minusDays(1), // Passado
                today.plusDays(2) // Reservado
        );

        // O dia de hoje e o dia 3, 4, 5 devem estar livres (não aparecem na lista)
        assertThat(blockedDates).doesNotContain(today.plusDays(3));
    }

    @Test
    @DisplayName("Deve lançar erro se data de fim for antes da de início")
    @Requirement("GF-49")
    void shouldThrowError_WhenDatesAreInverted() {
        LocalDate start = LocalDate.now().plusDays(5);
        LocalDate end = LocalDate.now().plusDays(2); // Fim antes do início

        assertThatThrownBy(() -> bookingService.getUnavailableDates(1L, start, end))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Deve lançar erro quando usuário não existe")
    void shouldThrowError_WhenUserNotFound() {
        LocalDate start = LocalDate.now().plusDays(5);
        LocalDate end = LocalDate.now().plusDays(7);

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.createBooking(999L, fishingRod.getId(), start, end))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Utilizador não encontrado");

        verify(bookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar erro quando item não existe")
    void shouldThrowError_WhenItemNotFound() {
        LocalDate start = LocalDate.now().plusDays(5);
        LocalDate end = LocalDate.now().plusDays(7);

        when(userRepository.findById(renter.getId())).thenReturn(Optional.of(renter));
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.createBooking(renter.getId(), 999L, start, end))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Item não encontrado");

        verify(bookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar erro quando usuário tenta alugar próprio item")
    void shouldThrowError_WhenUserTriesToRentOwnItem() {
        LocalDate start = LocalDate.now().plusDays(5);
        LocalDate end = LocalDate.now().plusDays(7);

        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(itemRepository.findById(fishingRod.getId())).thenReturn(Optional.of(fishingRod));
        // User is active (not suspended) - this check happens before the "own item"
        // check
        when(userService.isUserActive(owner.getId())).thenReturn(true);

        assertThatThrownBy(() -> bookingService.createBooking(owner.getId(), fishingRod.getId(), start, end))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Não podes alugar a tua própria cana");

        verify(bookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar erro quando booking não encontrado")
    void shouldThrowError_WhenBookingNotFoundForUpdate() {
        when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.updateBookingStatus(999L, BookingStatus.CONFIRMED, owner.getId()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Reserva nã encontrada");
    }

    @Test
    @DisplayName("Deve lançar erro ao tentar mudar para status inválido")
    void shouldThrowError_WhenInvalidStatusTransition() {
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        assertThatThrownBy(
                () -> bookingService.updateBookingStatus(booking.getId(), BookingStatus.ACTIVE, owner.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Só podes Confirmar ou Rejeitar");
    }

    @Test
    @DisplayName("Deve retornar bookings por userId")
    void shouldGetBookingsByUserId() {
        List<Booking> expected = List.of(booking);
        when(bookingRepository.findAllByUserId(renter.getId())).thenReturn(expected);

        List<Booking> result = bookingService.getBookingsByUserId(renter.getId());

        assertThat(result).hasSize(1);
        assertThat(result).containsExactly(booking);
        verify(bookingRepository, times(1)).findAllByUserId(renter.getId());
    }

    @Test
    @DisplayName("Deve retornar bookings por itemId")
    void shouldGetBookingsByItemId() {
        List<Booking> expected = List.of(booking);
        when(bookingRepository.findAllByItemId(fishingRod.getId())).thenReturn(expected);

        List<Booking> result = bookingService.getBookingsByItemId(fishingRod.getId());

        assertThat(result).hasSize(1);
        assertThat(result).containsExactly(booking);
        verify(bookingRepository, times(1)).findAllByItemId(fishingRod.getId());
    }

    @Test
    @DisplayName("Deve retornar bookings por item e mês")
    void shouldGetBookingsByItemAndMonth() {
        LocalDate start = LocalDate.of(2025, 12, 1);
        LocalDate end = LocalDate.of(2025, 12, 31);
        List<Booking> expected = List.of(booking);

        when(bookingRepository.findByItemIdAndDateRange(fishingRod.getId(), start, end)).thenReturn(expected);

        List<Booking> result = bookingService.getBookingsByItemAndMonth(fishingRod.getId(), 2025, 12);

        assertThat(result).hasSize(1);
        verify(bookingRepository, times(1)).findByItemIdAndDateRange(fishingRod.getId(), start, end);
    }

    @Test
    @DisplayName("Deve retornar bookings por item e semana")
    void shouldGetBookingsByItemAndWeek() {
        LocalDate weekStart = LocalDate.of(2025, 12, 15);
        LocalDate weekEnd = weekStart.plusDays(6);
        List<Booking> expected = List.of(booking);

        when(bookingRepository.findByItemIdAndDateRange(fishingRod.getId(), weekStart, weekEnd)).thenReturn(expected);

        List<Booking> result = bookingService.getBookingsByItemAndWeek(fishingRod.getId(), weekStart);

        assertThat(result).hasSize(1);
        verify(bookingRepository, times(1)).findByItemIdAndDateRange(fishingRod.getId(), weekStart, weekEnd);
    }

    @Test
    @DisplayName("Deve retornar booking por ID")
    void shouldGetBookingById() {
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        Optional<Booking> result = bookingService.getBooking(booking.getId());

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(booking);
    }

    @Test
    @DisplayName("Deve retornar todos os bookings")
    void shouldGetAllBookings() {
        List<Booking> expected = List.of(booking);
        when(bookingRepository.findAll()).thenReturn(expected);

        List<Booking> result = bookingService.getBookings();

        assertThat(result).hasSize(1);
        assertThat(result).containsExactly(booking);
    }

    @Test
    @DisplayName("Deve incluir datas bloqueadas em datas indisponíveis")
    void shouldIncludeBlockedDatesInUnavailable() {
        LocalDate today = LocalDate.now();
        LocalDate queryStart = today;
        LocalDate queryEnd = today.plusDays(5);

        BlockedDate blockedDate = new BlockedDate(today.plusDays(1), today.plusDays(2), "Manutenção", fishingRod);

        when(bookingRepository.findBookingsInRange(eq(fishingRod.getId()), any(), any()))
                .thenReturn(List.of());
        when(blockedDateRepository.findBlockedDatesInRange(eq(fishingRod.getId()), any(), any()))
                .thenReturn(List.of(blockedDate));

        List<LocalDate> unavailableDates = bookingService.getUnavailableDates(fishingRod.getId(), queryStart, queryEnd);

        assertThat(unavailableDates).contains(today.plusDays(1), today.plusDays(2));
    }
}