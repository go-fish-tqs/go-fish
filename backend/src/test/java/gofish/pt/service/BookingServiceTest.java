package gofish.pt.service;

import gofish.pt.entity.*;
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
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BookingService bookingService;

    // Dados de teste (para não repetir código)
    private User renter;
    private User owner;
    private Item fishingRod;
    private Booking booking;

    @BeforeEach
    void setup() {
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
    void shouldCreateBooking_WhenAvailable() {
        // Arrange (Preparar o terreno)
        LocalDate start = LocalDate.now().plusDays(5);
        LocalDate end = LocalDate.now().plusDays(7);

        when(userRepository.findById(renter.getId())).thenReturn(Optional.of(renter));
        when(itemRepository.findById(fishingRod.getId())).thenReturn(Optional.of(fishingRod));
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
    void shouldThrowError_WhenItemIsOccupied() {
        LocalDate start = LocalDate.now().plusDays(5);
        LocalDate end = LocalDate.now().plusDays(7);

        when(userRepository.findById(renter.getId())).thenReturn(Optional.of(renter));
        when(itemRepository.findById(fishingRod.getId())).thenReturn(Optional.of(fishingRod));

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
    void shouldThrowError_WhenNonOwnerTriesToConfirm() {
        // Arrange
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        // Act & Assert (O Zé (renter) tenta confirmar a reserva dele próprio -> PROIBIDO!)
        assertThatThrownBy(() -> bookingService.updateBookingStatus(booking.getId(), BookingStatus.CONFIRMED, renter.getId()))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    @DisplayName("Não deve deixar alterar uma reserva que já foi aceite")
    void shouldThrowError_WhenUpdatingConfirmedBooking() {
        // Arrange
        booking.setStatus(BookingStatus.CONFIRMED); // Já está aceite
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        // Act & Assert
        assertThatThrownBy(() -> bookingService.updateBookingStatus(booking.getId(), BookingStatus.CANCELLED, owner.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("já foi tratada");
    }

    // --- TESTES DE DISPONIBILIDADE (checkAvailability) ---

    @Test
    @DisplayName("Deve devolver lista de dias ocupados + dias passados")
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

        // Act
        List<LocalDate> blockedDates = bookingService.getUnavailableDates(fishingRod.getId(), queryStart, queryEnd);

        // Assert
        // Esperamos:
        // 1. O dia de Ontem (porque é passado)
        // 2. O dia da reserva existente (daqui a 2 dias)
        assertThat(blockedDates).contains(
                today.minusDays(1), // Passado
                today.plusDays(2)   // Reservado
        );

        // O dia de hoje e o dia 3, 4, 5 devem estar livres (não aparecem na lista)
        assertThat(blockedDates).doesNotContain(today.plusDays(3));
    }

    @Test
    @DisplayName("Deve lançar erro se data de fim for antes da de início")
    void shouldThrowError_WhenDatesAreInverted() {
        LocalDate start = LocalDate.now().plusDays(5);
        LocalDate end = LocalDate.now().plusDays(2); // Fim antes do início

        assertThatThrownBy(() -> bookingService.getUnavailableDates(1L, start, end))
                .isInstanceOf(IllegalArgumentException.class);
    }
}