package gofish.pt.mapper;

import gofish.pt.dto.ItemDTO;
import gofish.pt.entity.Item;
import gofish.pt.entity.User;
import gofish.pt.repository.UserRepository;
import gofish.pt.security.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

// componentModel = "spring" faz com que isto seja um @Bean (podes fazer @Autowired disto)
@Mapper(componentModel = "spring")
public abstract class ItemMapper {

    @Autowired
    protected UserRepository userRepository;

    // A regra: O campo 'owner' vem do utilizador autenticado usando o método 'getAuthenticatedUser'
    @Mapping(target = "owner", expression = "java(getAuthenticatedUser())")
    public abstract Item toEntity(ItemDTO dto);

    public abstract ItemDTO toDTO(Item item);

    // O método que obtém o utilizador autenticado do contexto de segurança
    protected User getAuthenticatedUser() {
        Long userId = SecurityUtils.getAuthenticatedUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Authenticated user not found with id: " + userId));
    }
}