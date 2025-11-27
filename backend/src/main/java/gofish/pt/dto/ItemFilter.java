package gofish.pt.dto;

import gofish.pt.entity.Category;
import gofish.pt.entity.Material;
import org.springframework.data.domain.Sort;

public record ItemFilter(
        String name,
        Category category,
        Material material,
        Double minPrice,
        Double maxPrice,
        String sortBy,
        Sort.Direction direction
) {}
