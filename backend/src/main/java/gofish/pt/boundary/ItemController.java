package gofish.pt.boundary;

import gofish.pt.dto.BlockDateRequestDTO;
import gofish.pt.dto.ItemDTO;
import gofish.pt.dto.ItemFilter;
import gofish.pt.dto.ItemUpdateDTO;
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
    public ResponseEntity<Item> getItem(@PathVariable Long id) {
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

    @PutMapping("/{id}")
    public ResponseEntity<Item> updateItem(
            @PathVariable Long id,
            @Valid @RequestBody ItemUpdateDTO updateDTO) {
        Long ownerId = getCurrentUserId();
        System.out.println("=== UPDATE ITEM DEBUG ===");
        System.out.println("Item ID: " + id);
        System.out.println("Owner ID from JWT: " + ownerId);
        System.out.println("Update DTO: " + updateDTO);
        Item updated = itemService.updateItem(id, updateDTO, ownerId);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/categories")
    public List<Category> getCategories() {
        return itemService.getCategories();
    }

    @GetMapping("/materials")
    public Map<Material.MaterialGroup, List<Material>> getMaterials() {
        return itemService.getMaterials();
    }

    // Get all items owned by the current user
    @GetMapping("/my")
    public ResponseEntity<List<Item>> getMyItems() {
        Long ownerId = getCurrentUserId();
        List<Item> items = itemService.findByOwnerId(ownerId);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/{id}/unavailability")
    public ResponseEntity<List<LocalDate>> checkAvailability(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        List<LocalDate> blockedDates = bookingService.getUnavailableDates(id, from, to);

        return ResponseEntity.ok(blockedDates);
    }

    @GetMapping("/{itemId}/blocked-dates")
    public ResponseEntity<List<BlockedDate>> getBlockedDates(
            @PathVariable Long itemId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        List<BlockedDate> blockedDates = itemService.getBlockedDates(itemId, from, to);
        return ResponseEntity.ok(blockedDates);
    }

    @PostMapping("/{itemId}/blocked-dates")
    public ResponseEntity<BlockedDate> blockDates(
            @PathVariable Long itemId,
            @Valid @RequestBody BlockDateRequestDTO request) {

        Long ownerId = getCurrentUserId();
        BlockedDate savedBlockedDate = itemService.blockDateRange(itemId, request, ownerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedBlockedDate);
    }

    @DeleteMapping("/blocked-dates/{blockedDateId}")
    public ResponseEntity<Void> unblockDate(@PathVariable Long blockedDateId) {
        Long ownerId = getCurrentUserId();
        itemService.unblockDateRange(blockedDateId, ownerId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Recupera o ID do utilizador autenticado através do Spring Security Context.
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        System.out.println("=== GET CURRENT USER DEBUG ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        System.out.println("Principal: " + (authentication != null ? authentication.getPrincipal() : "null"));
        System.out.println("Principal class: " + (authentication != null && authentication.getPrincipal() != null
                ? authentication.getPrincipal().getClass()
                : "null"));

        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }

        // O JWT retorna o userId diretamente como principal
        Object principal = authentication.getPrincipal();

        try {
            // Se o principal já é um Long (userId) - usado em produção com JWT
            if (principal instanceof Long) {
                System.out.println("Principal is Long: " + principal);
                return (Long) principal;
            }

            // Se for UserDetails (usado em testes com @WithMockUser)
            if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                String username = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
                System.out.println("Principal is UserDetails, username: " + username);
                User user = userRepository.findByUsername(username)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
                System.out.println("Found user ID: " + user.getId());
                return user.getId();
            }

            // Tenta converter String para Long (caso seja o userId como string)
            String principalStr = principal.toString();
            System.out.println("Principal as String: " + principalStr);
            try {
                Long userId = Long.parseLong(principalStr);
                System.out.println("Parsed userId: " + userId);
                return userId;
            } catch (NumberFormatException e) {
                // Se não for número, assume que é username
                System.out.println("Not a number, treating as username");
                User user = userRepository.findByUsername(principalStr)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
                System.out.println("Found user ID: " + user.getId());
                return user.getId();
            }
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            System.out.println("Error getting user ID: " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Invalid user authentication: " + e.getMessage());
        }
    }
}
