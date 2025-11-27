package gofish.pt.boundary;

import gofish.pt.entity.Item;
import gofish.pt.entity.ItemDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import gofish.pt.service.ItemService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService itemService;

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping
    public List<Item> getItems() {
        return itemService.findAll();
    }



    @PostMapping
    public ResponseEntity<Item> createItem(@Valid @RequestBody ItemDTO dto) {
        Item item = itemService.fromDto(dto);
        Item saved = itemService.save(item);
        URI location = URI.create("/api/items/" + saved.getId());
        return ResponseEntity.created(location).body(saved);
    }


}
