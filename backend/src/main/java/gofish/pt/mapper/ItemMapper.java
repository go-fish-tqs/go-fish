package gofish.pt.mapper;

import gofish.pt.dto.ItemDTO;
import gofish.pt.entity.Item;
import gofish.pt.entity.User;
import gofish.pt.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

// componentModel = "spring" faz com que isto seja um @Bean (podes fazer @Autowired disto)
@Mapper(componentModel = "spring")
public abstract class ItemMapper {

    @Autowired
    protected UserRepository userRepository;

    // A regra: O campo 'owner' vem do 'userId' usando o método 'mapUser'
    @Mapping(target = "owner", source = "userId", qualifiedByName = "idToUser")
    public abstract Item toEntity(ItemDTO dto);

    // Podes ter o contrário também, para quando mandas dados para o frontend
    @Mapping(target = "userId", source = "owner.id")
    public abstract ItemDTO toDTO(Item item);

    // O método artesanal que vai buscar o User à base de dados
    @org.mapstruct.Named("idToUser")
    protected User mapUser(Long id) {
        if (id == null) {
            throw new EntityNotFoundException("userId is required to create an item");
        }
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
    }
}