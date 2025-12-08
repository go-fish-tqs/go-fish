package gofish.pt.boundary;

import gofish.pt.dto.ItemDTO;
import gofish.pt.dto.ItemFilter;
import gofish.pt.entity.Category;
import gofish.pt.entity.Item;
import gofish.pt.entity.Material;
import gofish.pt.service.BookingService;
import gofish.pt.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService itemService;
    private final BookingService bookingService;

    @Autowired
    public ItemController(ItemService itemService, BookingService bookingService) {
        this.itemService = itemService;
        this.bookingService = bookingService;
    }


    @PostMapping("/filter")
    public List<Item> getItems(@Valid @RequestBody(required = false) ItemFilter filter) {
        return itemService.findAll(filter);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Item> getItem(@PathVariable Integer id) {
        return itemService.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
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

    @GetMapping("/{id}/availability")
    public ResponseEntity<List<LocalDate>> checkAvailability(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        // Converter LocalDate (dia) para LocalDateTime (momento exato)
        // From: Começa às 00:00
        LocalDateTime startDateTime = from.atStartOfDay();
        // To: Acaba às 23:59:59.999999999
        LocalDateTime endDateTime = to.atTime(23, 59, 59);

        List<LocalDate> blockedDates = bookingService.checkAvailability(id, startDateTime, endDateTime);

        return ResponseEntity.ok(blockedDates);
    }

}
