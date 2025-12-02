package gofish.pt.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gofish.pt.entity.Category;
import gofish.pt.entity.Material;
import org.springframework.data.domain.Sort;

public record ItemFilter(
        String name,

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Category category,
        Material material,
        Double minPrice,
        Double maxPrice,
        String sortBy,
        Sort.Direction direction
) {}