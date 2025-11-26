package gofish.pt.service;

import gofish.pt.entity.Item;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import gofish.pt.repository.ItemRepository;

import java.util.List;
import java.util.Optional;

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

    public List<Item> findAll(Specification<Item> spec) {
        return repository.findAll(spec);
    }

    public Item save(Item item) {
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
}
