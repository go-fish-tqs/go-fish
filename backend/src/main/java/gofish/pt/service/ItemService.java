package gofish.pt.service;

import gofish.pt.dto.ItemFilter;
import gofish.pt.entity.Category;
import gofish.pt.entity.Item;
import gofish.pt.dto.ItemDTO;
import gofish.pt.entity.Material;
import gofish.pt.mapper.ItemMapper;
import gofish.pt.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static gofish.pt.repository.ItemSpecifications.*;

import static gofish.pt.repository.ItemSpecifications.*;

@Service
@Transactional
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository repository;
    private final ItemMapper itemMapper;

    public Optional<Item> findById(long id) {
        return repository.findById(id);
    }

    public List<Item> findAll() {
        return repository.findAll();
    }

    public List<Item> findAll(ItemFilter filter) {

        if (filter == null)
            return findAll();

        Specification<Item> spec = Specification.allOf(nameContains(filter.name()),
                categoryIs(filter.category()),
                materialIs(filter.material()),
                priceBetween(filter.minPrice(), filter.maxPrice()));

        String sortBy = (filter.sortBy() != null) ? filter.sortBy() : "id";
        Sort.Direction direction = (filter.direction() != null) ? filter.direction() : Sort.Direction.ASC;

        Sort sort = Sort.by(direction, sortBy);

        return repository.findAll(spec, sort);
    }

    public Item save(ItemDTO dto) {
        Item item = itemMapper.toEntity(dto);

        if (item == null)
            return null;
        return repository.save(item);
    }

    public void delete(Item item) {
        if (item != null)
            repository.delete(item);
    }

    public boolean exists(long id) {
        return repository.existsById(id);
    }

    public List<Category> getCategories() {
        return Arrays.stream(Category.values())
                .filter(Category::isTopLevel)
                .toList();
    }

    public Map<Material.MaterialGroup, List<Material>> getMaterials() {
        Map<Material.MaterialGroup, List<Material>> materials = new HashMap<>();
        for (Material.MaterialGroup group : Material.MaterialGroup.values()) {
            materials.put(group, group.getMaterials());
        }
        return materials;
    }

}
