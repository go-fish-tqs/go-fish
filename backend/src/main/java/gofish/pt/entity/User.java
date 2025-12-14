package gofish.pt.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @Column(nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 32)
    private String username;

    @NotBlank
    @Column(nullable = false, length = 64, unique = true)
    private String email;

    @NotBlank
    @Column(nullable = false)
    private String password;

    @NotBlank
    @Column(nullable = false)
    private String location;

    @NotNull
    @Column(nullable = false)
    private Double balance = 0.0;

    @OneToMany
    @JoinColumn(name = "owner_id")
    @JsonIgnore
    private java.util.List<Item> items = new ArrayList<>();

    public void addItem(Item item) {
        items.add(item);
        item.setOwner(this); // <--- O SEGREDO ESTÁ AQUI!
    }

    public void removeItem(Item item) {
        items.remove(item);
        item.setOwner(null);
    }

    @OneToMany
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private java.util.List<Booking> bookings = new ArrayList<>();

    public void addBooking(Booking booking) {
        bookings.add(booking);
        booking.setUser(this); // <--- O SEGREDO ESTÁ AQUI!
    }

    public void removeBooking(Booking booking) {
        bookings.remove(booking);
        booking.setUser(null);
    }

    @JsonIgnore
    public List<Booking> getOwnedBookings() {
        return items.stream().map(Item::getBookings)
                .flatMap(List::stream)
                .toList();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        // Se ambos tiverem ID, compara IDs. Se não, compara referências.
        return getId() != null && getId().equals(user.getId());
    }

    @Override
    public int hashCode() {
        // Retorna um valor constante ou baseado na classe para evitar problemas com HashSets
        return getClass().hashCode();
    }
    @OneToMany
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private java.util.List<Review> reviews = new ArrayList<>();

}
