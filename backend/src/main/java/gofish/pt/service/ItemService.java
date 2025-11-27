package gofish.pt.service;

import gofish.pt.dto.ItemFilter;
import gofish.pt.entity.Category;
import gofish.pt.entity.Item;
import gofish.pt.dto.ItemDTO;
import gofish.pt.entity.Material;
import gofish.pt.repository.ItemRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static gofish.pt.repository.ItemSpecifications.*;

@Service
@Transactional
public class ItemService {

    private final ItemRepository repository;

    public ItemService(ItemRepository repository) {
        this.repository = repository;
    }

    public Optional<Item> findById(long id) {
        return repository.findById(id);
    }

    public List<Item> findAll() {
        return repository.findAll();
    }

    public List<Item> findAll(ItemFilter filter) {

        if (filter == null) return findAll();

        Specification<Item> spec = Specification.allOf(nameContains(filter.name()),
                categoryIs(filter.category()),
                materialIs(filter.material()),
                priceBetween(filter.minPrice(), filter.maxPrice()));

        String sortBy = (filter.sortBy() != null) ? filter.sortBy() : "id";
        Sort.Direction direction = (filter.direction() != null) ? filter.direction() : Sort.Direction.ASC;

        Sort sort = Sort.by(direction, sortBy);

        return repository.findAll(spec, sort);
    }

    public Item save(Item item) {
        if (item == null) return null;
        return repository.save(item);
    }

    public void delete(Item item) {
        if (item != null) repository.delete(item);
    }

    public boolean exists(long id) {
        return repository.existsById(id);
    }

    public Item fromDTO(ItemDTO dto) {
        Item item = new Item();
        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setPhotoUrls(dto.getPhotoUrls());
        item.setCategory(dto.getCategory());
        item.setMaterial(dto.getMaterial());
        item.setPrice(dto.getPrice());
        item.setUserId(dto.getUserId());
        return item;
    }

}
