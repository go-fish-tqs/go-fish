package gofish.pt.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "items")
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
    @Column(name = "photo_url", columnDefinition = "TEXT")
    private List<String> photoUrls = new ArrayList<>();

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
    private Boolean available = true;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    @JsonIgnoreProperties({ "email", "password", "location", "balance" })
    private User owner;

    @OneToMany
    @JoinColumn(name = "item_id")
    @JsonIgnore
    private List<Booking> bookings = new ArrayList<>();

    public void addBooking(Booking booking) {
        bookings.add(booking);
        booking.setItem(this); // <--- O SEGREDO ESTÁ AQUI!
    }

    public void removeBooking(Booking booking) {
        bookings.remove(booking);
        booking.setItem(null);
    }

    @OneToMany
    @JoinColumn(name = "item_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<Review> reviews = new java.util.ArrayList<>();
}
