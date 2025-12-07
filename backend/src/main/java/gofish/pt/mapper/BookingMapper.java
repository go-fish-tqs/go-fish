package gofish.pt.mapper;

import gofish.pt.dto.BookingRequestDTO;
import gofish.pt.dto.BookingResponseDTO;
import gofish.pt.entity.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    // --- De DTO para Entidade ---
    // Nota: Como o teu Service pede (userId, itemId, start, end) separadamente,
    // se calhar nem precisas de converter o RequestDTO para Entity aqui,
    // o Controller desenpacota. Mas se precisares, seria algo assim:
    @Mapping(target = "item", ignore = true) // O Service é que busca o Item
    @Mapping(target = "user", ignore = true) // O Service é que busca o User
    @Mapping(target = "status", constant = "PENDING") // Começa sempre Pendente
    Booking toEntity(BookingRequestDTO dto);


    // --- De Entidade para DTO (O mais importante) ---
    @Mapping(target = "itemId", source = "item.id")
    @Mapping(target = "itemName", source = "item.name")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", source = "user.username") // Supondo que User tem 'name'
    @Mapping(target = "totalPrice", source = ".", qualifiedByName = "calculatePrice")
    @Mapping(target = "itemPhotoUrl", source = "item.photoUrls", qualifiedByName = "getFirstPhoto")
    BookingResponseDTO toDTO(Booking booking);

    // Método artesanal para garantir que o preço vai preenchido
    @Named("calculatePrice")
    default Double calculatePrice(Booking booking) {
        if (booking.getItem() == null || booking.getStartDate() == null || booking.getEndDate() == null) {
            return 0.0;
        }
        return booking.getTotalPrice(); // Aquele método que já tinhas na Entidade
    }

    @Named("getFirstPhoto")
    default String getFirstPhoto(java.util.List<String> photos) {
        if (photos != null && !photos.isEmpty()) {
            return photos.get(0);
        }
        return null;
    }
}