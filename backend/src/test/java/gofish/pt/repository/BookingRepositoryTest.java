package gofish.pt.repository;

import app.getxray.xray.junit.customjunitxml.annotations.Requirement;
import gofish.pt.entity.*; // Importa as tuas entidades todas
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest // <--- O segredo para testar repositórios
class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    private Item fishingRod;
    private User zePescador;

    @BeforeEach
    void setup() {
        zePescador = new User();
        zePescador.setUsername("Zé do Pipo");
        zePescador.setEmail("ze@gofish.pt");
        zePescador.setPassword("12345");
        zePescador.setLocation("Faro");
        userRepository.save(zePescador);

        // 2. Criar e salvar um Item
        fishingRod = new Item();
        fishingRod.setName("Cana XPTO");
        fishingRod.setDescription("Pesca tudo");
        fishingRod.setPrice(10.0);
        fishingRod.setCategory(Category.RODS);
        fishingRod.setMaterial(Material.CARBON_FIBER);
        fishingRod.setOwner(zePescador); // O dono do item é o mesmo user pra simplificar
        itemRepository.save(fishingRod);
    }

    // --- TESTES DE SOBREPOSIÇÃO (Overlapping) ---

    @Test
    @DisplayName("Deve detetar conflito quando as datas são exatamente iguais")
    @Requirement("GF-51")
    void shouldFindOverlap_WhenDatesAreExact() {
        // Arrange: Já existe uma reserva de dia 10 a 12
        createBooking(LocalDate.of(2025, 1, 10), LocalDate.of(2025, 1, 12), BookingStatus.CONFIRMED);

        // Act: Tento reservar nas mesmas datas
        boolean exists = bookingRepository.existsOverlappingBooking(
                fishingRod.getId(),
                LocalDate.of(2025, 1, 10),
                LocalDate.of(2025, 1, 12)
        );

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Deve detetar conflito quando a nova reserva está DENTRO da existente")
    @Requirement("GF-51")
    void shouldFindOverlap_WhenNewIsInsideExisting() {
        // Arrange: Reserva de 10 a 20
        createBooking(LocalDate.of(2025, 1, 10), LocalDate.of(2025, 1, 20), BookingStatus.CONFIRMED);

        // Act: Tento reservar de 12 a 15 (está no meio)
        boolean exists = bookingRepository.existsOverlappingBooking(
                fishingRod.getId(),
                LocalDate.of(2025, 1, 12),
                LocalDate.of(2025, 1, 15)
        );

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("NÃO deve detetar conflito se a reserva existente estiver CANCELADA")
    @Requirement("GF-48")
    void shouldNotFindOverlap_WhenExistingIsCancelled() {
        // Arrange: Reserva de 10 a 12, mas CANCELADA
        createBooking(LocalDate.of(2025, 1, 10), LocalDate.of(2025, 1, 12), BookingStatus.CANCELLED);

        // Act: Tento reservar nas mesmas datas
        boolean exists = bookingRepository.existsOverlappingBooking(
                fishingRod.getId(),
                LocalDate.of(2025, 1, 10),
                LocalDate.of(2025, 1, 12)
        );

        // Assert
        assertThat(exists).isFalse(); // Deve deixar passar!
    }

    @Test
    @DisplayName("NÃO deve detetar conflito se as datas apenas se tocam (Fim = Início)")
    @Requirement("GF-48")
    void shouldNotFindOverlap_WhenDatesTouch() {
        // Arrange: Reserva acaba dia 10 às 12:00
        createBooking(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 10), BookingStatus.CONFIRMED);

        // Act: Nova reserva começa dia 10 às 12:00
        boolean exists = bookingRepository.existsOverlappingBooking(
                fishingRod.getId(),
                LocalDate.of(2025, 1, 10),
                LocalDate.of(2025, 1, 15)
        );

        // Assert
        assertThat(exists).isFalse(); // Tocar não é sobrepor!
    }

    // --- TESTES DE BUSCA POR INTERVALO (Calendar) ---

    @Test
    @DisplayName("Deve encontrar reservas confirmadas dentro de um intervalo")
    @Requirement("GF-51")
    void shouldFindBookingsInRange() {
        // Arrange
        // Reserva 1: Dia 5 a 10 (CONFIRMADA)
        createBooking(LocalDate.of(2025, 1, 5), LocalDate.of(2025, 1, 10), BookingStatus.CONFIRMED);
        // Reserva 2: Dia 20 a 25 (PENDENTE - Depende da tua query se queres mostrar ou nã)
        createBooking(LocalDate.of(2025, 1, 20), LocalDate.of(2025, 1, 25), BookingStatus.PENDING);
        // Reserva 3: Dia 1 a 2 (Fora do range que vamos pedir)
        createBooking(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 2), BookingStatus.CONFIRMED);

        // Act: Pedir reservas para Janeiro todo (Dia 1 a 31)
        List<Booking> found = bookingRepository.findBookingsInRange(
                fishingRod.getId(),
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 1, 31)
        );

        // Assert
        // Nota: Se a tua query filtrar só CONFIRMED, espera 2 (a primeira e a terceira).
        // Se a query filtrar CONFIRMED e PENDING, espera as 3 primeiras.
        // Vou assumir que a tua query 'findBookingsInRange' só traz CONFIRMED como falámos antes.

        assertThat(found).hasSize(2)
                .extracting(Booking::getStatus)
                .containsOnly(BookingStatus.CONFIRMED);
    }

    // --- Helper Method pra nã repetir código ---
    private Booking createBooking(LocalDate start, LocalDate end, BookingStatus status) {
        Booking b = new Booking();
        b.setItem(fishingRod);
        b.setUser(zePescador);
        b.setStartDate(start);
        b.setEndDate(end);
        b.setStatus(status);
        b.setPrice(calculatePrice(start, end, fishingRod.getPrice()));
        return bookingRepository.save(b);
    }

    private Double calculatePrice(LocalDate start, LocalDate end, Double price){
        long days = java.time.temporal.ChronoUnit.DAYS.between(start, end);
        if (days == 0) days = 1;
        return days * price;
    }
}