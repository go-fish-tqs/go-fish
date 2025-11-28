package gofish.pt.boundary;

import gofish.pt.dto.ItemDTO;
import gofish.pt.dto.ItemFilter;
import gofish.pt.entity.Category;
import gofish.pt.entity.Item;
import gofish.pt.entity.Material;
import gofish.pt.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/filter")
    public List<Item> getItems(@Valid @RequestBody(required = false) ItemFilter filter) {
        return itemService.findAll(filter);
    }


    @PostMapping
    public ResponseEntity<Item> createItem(@Valid @RequestBody ItemDTO dto) {
        Item item = itemService.fromDTO(dto);
        Item saved = itemService.save(item);
        URI location = URI.create("/api/items/" + saved.getId());
        return ResponseEntity.created(location).body(saved);
    }


}
