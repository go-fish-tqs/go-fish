package gofish.pt.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity(name = "items")
public class Item {

    // Attributes

    @Id
    @Column(nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 64)
    private String name;

    @NotBlank
    @Column(nullable = false, length = 512)
    private String description;

    @ElementCollection
    @CollectionTable(name = "item_photos", joinColumns = @JoinColumn(name = "item_id"))
    @Column(name = "photo_url", columnDefinition = "CLOB")
    private List<String> photoUrls;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @JsonIgnoreProperties("subCategories")
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Material material;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private Boolean available;

    @Column(nullable = false)
    private Long userId;

    // Constructors

    // item is available by default
    public Item() {
        available = true;
    }

    public Item(Long userId, String name, String description, List<String> photoUrls, Material material, Category category, Double price) {
        this();
        this.userId = userId;
        this.name = name;
        this.description = description;
        this.photoUrls = photoUrls;
        this.material = material;
        this.category = category;
        this.price = price;
    }

}
