package gofish.pt.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

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
    @Column(name = "photo_url")
    private List<String> photoUrls;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
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

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getPhotoUrls() {
        return photoUrls;
    }

    public void setPhotoUrls(List<String> photoUrls) {
        this.photoUrls = photoUrls;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }
}
