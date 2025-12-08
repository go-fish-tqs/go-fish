package gofish.pt.mapper;

import gofish.pt.dto.ItemDTO;
import gofish.pt.entity.Category;
import gofish.pt.entity.Item;
import gofish.pt.entity.Material;
import gofish.pt.entity.User;
import gofish.pt.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // <--- Liga o Mockito para injetar coisas
class ItemMapperTest {

    @Mock
    private UserRepository userRepository;

    // O TRUQUE: Injetamos o mock na implementação GERADA pelo MapStruct
    // Se der erro aqui, corre 'mvn compile' primeiro!
    @InjectMocks
    private ItemMapperImpl itemMapper;

    @Test
    @DisplayName("Deve converter DTO para Entidade e buscar o Dono (User) na BD")
    void shouldMapToEntity_AndFetchOwner() {
        // Arrange
        Long userId = 50L;
        User user = new User();
        user.setId(userId);
        user.setUsername("ze_do_anzol");

        // O nosso DTO
        ItemDTO dto = new ItemDTO(
                "Cana XPTO",
                "Descrição fixe",
                List.of("foto1.jpg"),
                Category.RODS,
                Material.CARBON_FIBER,
                15.0,
                userId // <--- O ID que vamos usar para buscar o user
        );

        // Ensinar o Mock: "Quando pedirem o user 50, dá este aqui"
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        Item item = itemMapper.toEntity(dto);

        // Assert
        assertThat(item).isNotNull();
        assertThat(item.getName()).isEqualTo("Cana XPTO");
        assertThat(item.getPrice()).isEqualTo(15.0);

        // Verifica se o mapeamento especial funcionou
        assertThat(item.getOwner()).isNotNull();
        assertThat(item.getOwner().getId()).isEqualTo(userId);
        assertThat(item.getOwner().getUsername()).isEqualTo("ze_do_anzol");

        // Confirma que o método foi chamado
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Deve converter Entidade para DTO e extrair o ID do Dono")
    void shouldMapToDTO_AndExtractOwnerId() {
        // Arrange
        User owner = new User();
        owner.setId(99L);

        Item item = new Item();
        item.setId(1L);
        item.setName("Isco Vivo");
        item.setPrice(2.5);
        item.setOwner(owner); // <--- O item tem dono

        // Act
        ItemDTO dto = itemMapper.toDTO(item);

        // Assert
        assertThat(dto).isNotNull();
        assertThat(dto.getName()).isEqualTo("Isco Vivo");
        // Verifica se o userId foi preenchido com o ID do owner
        assertThat(dto.getUserId()).isEqualTo(99L);
    }

    @Test
    @DisplayName("Deve deixar o owner null se o userId não existir na BD")
    void shouldMapToEntity_WithNullOwner_WhenUserNotFound() {
        // Arrange
        Long nonExistentId = 999L;
        ItemDTO dto = new ItemDTO();
        dto.setUserId(nonExistentId); // ID que nã existe
        // Preencher o resto pra nã dar null pointer noutros lados se for preciso
        dto.setName("Coisa");

        // O repositório diz que não há ninguém
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act
        Item item = itemMapper.toEntity(dto);

        // Assert
        assertThat(item).isNotNull();
        // Como fizeste .orElse(null) no mapper, o owner deve ser null
        assertThat(item.getOwner()).isNull();
    }

    @Test
    @DisplayName("Não deve chamar o repositório se o userId for null no DTO")
    void shouldNotCallRepo_WhenUserIdIsNull() {
        // Arrange
        ItemDTO dto = new ItemDTO();
        dto.setUserId(null); // Sem ID
        dto.setName("Item Orfão");

        // Act
        Item item = itemMapper.toEntity(dto);

        // Assert
        assertThat(item.getOwner()).isNull();
        // Garante que nem sequer foi incomodar a base de dados
        verify(userRepository, never()).findById(any());
    }
}