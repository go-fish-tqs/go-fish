package gofish.pt.boundary;

import gofish.pt.dto.BlockDateRequestDTO;
import gofish.pt.dto.ItemDTO;
import gofish.pt.dto.ItemFilter;
import gofish.pt.entity.*;
import gofish.pt.repository.UserRepository; // Importar Repositório
import gofish.pt.service.BookingService;
import gofish.pt.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication; // Importar
import org.springframework.security.core.context.SecurityContextHolder; // Importar
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException; // Importar

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService itemService;
    private final BookingService bookingService;
    private final UserRepository userRepository; // Adicionar Repositório

    @Autowired
    public ItemController(ItemService itemService, 
                          BookingService bookingService, 
                          UserRepository userRepository) { // Injetar Repositório
        this.itemService = itemService;
        this.bookingService = bookingService;
        this.userRepository = userRepository;
    }


    @PostMapping("/filter")
    public List<Item> getItems(@Valid @RequestBody(required = false) ItemFilter filter) {
        return itemService.findAll(filter);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Item> getItem(@PathVariable Integer id) {
        return itemService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Item> createItem(@Valid @RequestBody ItemDTO item) {
        Item saved = itemService.save(item);
        URI location = URI.create("/api/items/" + saved.getId());
        return ResponseEntity.created(location).body(saved);
    }

    @GetMapping("/categories")
    public List<Category> getCategories() {
        return itemService.getCategories();
    }

    @GetMapping("/materials")
    public Map<Material.MaterialGroup, List<Material>> getMaterials() {
        return itemService.getMaterials();
    }

    @GetMapping("/{id}/unavailability")
    public ResponseEntity<List<LocalDate>> checkAvailability(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        LocalDateTime startDateTime = from.atStartOfDay();
        LocalDateTime endDateTime = to.atTime(23, 59, 59);

        List<LocalDate> blockedDates = bookingService.checkAvailability(id, startDateTime, endDateTime);

        return ResponseEntity.ok(blockedDates);
    }

    @PostMapping("/{itemId}/blocked-dates")
    public ResponseEntity<BlockedDate> blockDates(
            @PathVariable Long itemId,
            @Valid @RequestBody BlockDateRequestDTO request) {
        
        // CORREÇÃO: Usar o ID real do utilizador logado
        Long ownerId = getCurrentUserId();
        
        BlockedDate savedBlockedDate = itemService.blockDateRange(itemId, request, ownerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedBlockedDate);
    }

    @DeleteMapping("/blocked-dates/{blockedDateId}")
    public ResponseEntity<Void> unblockDate(@PathVariable Long blockedDateId) {
        
        // CORREÇÃO: Usar o ID real do utilizador logado
        Long ownerId = getCurrentUserId();
        
        itemService.unblockDateRange(blockedDateId, ownerId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Recupera o ID do utilizador autenticado através do Spring Security Context.
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }

        // O .getName() retorna o username (ex: "ze_pescador")
        String username = authentication.getName();

        // Vamos à base de dados buscar o ID correspondente a este username
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        
        return user.getId();
    }
}
