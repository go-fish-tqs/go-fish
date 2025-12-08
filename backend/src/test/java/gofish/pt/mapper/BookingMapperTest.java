package gofish.pt.mapper;

import gofish.pt.dto.BookingRequestDTO;
import gofish.pt.dto.BookingResponseDTO;
import gofish.pt.entity.Booking;
import gofish.pt.entity.BookingStatus;
import gofish.pt.entity.Item;
import gofish.pt.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BookingMapperTest {

    // Instanciamos o Mapper manualmente (o MapStruct gera a implementação)
    // Se o IDE refilar com o Mappers.getMapper, garante que fizeste 'mvn clean compile'
    private final BookingMapper mapper = Mappers.getMapper(BookingMapper.class);

    @Test
    @DisplayName("Deve converter Entity para DTO e calcular o preço corretamente (vários dias)")
    void shouldMapToDTO_AndCalculatePrice() {
        // Arrange
        User user = new User();
        user.setId(10L);
        user.setUsername("ze_pescador");

        Item item = new Item();
        item.setId(5L);
        item.setName("Barco Valente");
        item.setPrice(100.0); // 100 paus por dia
        item.setPhotoUrls(List.of("url1.jpg", "url2.jpg"));

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setUser(user);
        booking.setItem(item);
        // De dia 1 a dia 5 = 4 dias de intervalo (no ChronoUnit.DAYS)
        booking.setStartDate(LocalDateTime.of(2025, 1, 1, 10, 0));
        booking.setEndDate(LocalDateTime.of(2025, 1, 5, 10, 0));

        // Act
        BookingResponseDTO dto = mapper.toDTO(booking);

        // Assert
        assertThat(dto).isNotNull();
        assertThat(dto.getUserId()).isEqualTo(10L);
        assertThat(dto.getUserName()).isEqualTo("ze_pescador");
        assertThat(dto.getItemId()).isEqualTo(5L);
        assertThat(dto.getItemName()).isEqualTo("Barco Valente");
        assertThat(dto.getItemPhotoUrl()).isEqualTo("url1.jpg"); // Confirmar que apanhou a primeira foto

        // A Prova dos Nove (Preço): 4 dias * 100.0 = 400.0
        assertThat(dto.getPrice()).isEqualTo(400.0);
    }

    @Test
    @DisplayName("Deve cobrar pelo menos 1 dia se a reserva for de poucas horas")
    void shouldCalculateMinimumPrice_WhenLessThanOneDay() {
        // Arrange
        Booking booking = new Booking();
        Item item = new Item();
        item.setPrice(50.0);
        booking.setItem(item);

        // Entra às 10h e sai às 15h do mesmo dia (0 dias completos)
        booking.setStartDate(LocalDateTime.of(2025, 1, 1, 10, 0));
        booking.setEndDate(LocalDateTime.of(2025, 1, 1, 15, 0));

        // Act
        Double price = mapper.calculatePrice(booking); // Testar o método default diretamente

        // Assert
        // Regra de negócio: Se days == 0, cobra 1 dia.
        assertThat(price).isEqualTo(50.0);
    }

    @Test
    @DisplayName("Deve converter RequestDTO para Entity e definir status PENDING")
    void shouldMapToEntity_WithPendingStatus() {
        // Arrange
        BookingRequestDTO request = new BookingRequestDTO();
        request.setUserId(99L); // Nota: O mapper ignora isto no toEntity, quem trata é o service
        request.setItemId(88L); // Idem
        request.setStartDate(LocalDateTime.now());
        request.setEndDate(LocalDateTime.now().plusDays(1));

        // Act
        Booking entity = mapper.toEntity(request);

        // Assert
        assertThat(entity).isNotNull();
        // O status tem de nascer PENDING, conforme a anotação @Mapping(constant = "PENDING")
        assertThat(entity.getStatus()).isEqualTo(BookingStatus.PENDING);

        // Confirmar que ignorou User e Item (vêm nulls do mapper, o Service é que os busca)
        assertThat(entity.getUser()).isNull();
        assertThat(entity.getItem()).isNull();
    }

    @Test
    @DisplayName("Deve lidar com listas de fotos vazias ou nulas sem rebentar")
    void shouldHandleEmptyPhotos() {
        // Arrange
        Booking booking = new Booking();
        Item item = new Item();
        item.setPhotoUrls(null); // ou List.of()
        booking.setItem(item);

        // Act
        BookingResponseDTO dto = mapper.toDTO(booking);

        // Assert
        assertThat(dto.getItemPhotoUrl()).isNull();
    }

    @Test
    @DisplayName("Deve devolver preço 0 se faltarem dados")
    void shouldReturnZeroPrice_WhenMissingData() {
        Booking booking = new Booking();
        // Falta item e datas

        assertThat(mapper.calculatePrice(booking)).isEqualTo(0.0);
    }
}