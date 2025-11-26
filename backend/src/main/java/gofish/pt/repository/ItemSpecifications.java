package gofish.pt.repository;

import gofish.pt.entity.Category;
import gofish.pt.entity.Item;
import gofish.pt.entity.Material;
import org.springframework.data.jpa.domain.Specification;

public class ItemSpecifications {

    public static Specification<Item> nameContains(String text) {
        return (root, query, builder) ->
                text == null ? null :
                        builder.like(
                                builder.lower(root.get("name")),
                                "%" + text.toLowerCase() + "%"
                        );
    }

    public static Specification<Item> categoryIs(Category category) {
        return (root, query, builder) ->
                category == null ? null :
                        builder.equal(root.get("category"), category);
    }

    public static Specification<Item> materialIs(Material material) {
        return (root, query, builder) ->
                material == null ? null :
                        builder.equal(root.get("material"), material);
    }

    public static Specification<Item> priceBetween(Double min, Double max) {
        return (root, query, builder) -> {
            if (min == null && max == null) return null;

            if (min == null)
                return builder.lessThanOrEqualTo(root.get("price"), max);

            if (max == null)
                return builder.greaterThanOrEqualTo(root.get("price"), min);

            return builder.between(root.get("price"), min, max);
        };
    }

    public static Specification<Item> availableIs(Boolean available) {
        return (root, query, builder) ->
                available == null ? null :
                        builder.equal(root.get("available"), available);
    }
}
