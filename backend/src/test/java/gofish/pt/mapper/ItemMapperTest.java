package gofish.pt.mapper;

import gofish.pt.dto.ItemDTO;
import gofish.pt.entity.Category;
import gofish.pt.entity.Item;
import gofish.pt.entity.Material;
import gofish.pt.entity.User;
import gofish.pt.repository.UserRepository;
import gofish.pt.security.TestSecurityContextHelper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class ItemMapperTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ItemMapperImpl itemMapper;

    @Test
    @DisplayName("Deve converter DTO para Entidade e buscar o Dono autenticado")
    void shouldMapToEntity_AndFetchAuthenticatedOwner() {
        // Arrange
        Long userId = 50L;
        User user = new User();
        user.setId(userId);
        user.setUsername("ze_do_anzol");

        // Configura o contexto de segurança com o utilizador autenticado
        TestSecurityContextHelper.setAuthenticatedUser(userId);

        ItemDTO dto = new ItemDTO(
                "Cana XPTO",
                "Descrição fixe",
                List.of("foto1.jpg"),
                Category.RODS,
                Material.CARBON_FIBER,
                15.0
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        Item item = itemMapper.toEntity(dto);

        // Assert
        assertThat(item).isNotNull();
        assertThat(item.getName()).isEqualTo("Cana XPTO");
        assertThat(item.getPrice()).isEqualTo(15.0);

        // Verifica se o owner foi preenchido com o utilizador autenticado
        assertThat(item.getOwner()).isNotNull();
        assertThat(item.getOwner().getId()).isEqualTo(userId);
        assertThat(item.getOwner().getUsername()).isEqualTo("ze_do_anzol");

        verify(userRepository).findById(userId);
        
        // Limpar o contexto
        TestSecurityContextHelper.clearContext();
    }

    @Test
    @DisplayName("Deve converter Entidade para DTO")
    void shouldMapToDTO() {
        // Arrange
        User owner = new User();
        owner.setId(99L);

        Item item = new Item();
        item.setId(1L);
        item.setName("Isco Vivo");
        item.setPrice(2.5);
        item.setOwner(owner);

        // Act
        ItemDTO dto = itemMapper.toDTO(item);

        // Assert
        assertThat(dto).isNotNull();
        assertThat(dto.getName()).isEqualTo("Isco Vivo");
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException se o utilizador autenticado não existir na BD")
    void shouldThrowException_WhenAuthenticatedUserNotFound() {
        // Arrange
        Long nonExistentId = 999L;
        TestSecurityContextHelper.setAuthenticatedUser(nonExistentId);
        
        ItemDTO dto = new ItemDTO();
        dto.setName("Coisa");

        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> itemMapper.toEntity(dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Authenticated user not found with id: 999");
        
        TestSecurityContextHelper.clearContext();
    }
}