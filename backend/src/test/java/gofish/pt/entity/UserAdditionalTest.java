package gofish.pt.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class UserAdditionalTest {

    @Test
    void defaultBalance_isZero_and_addRemoveItem_setsOwner() {
        User u = new User();
        u.setUsername("anna");
        u.setEmail("a@b.com");
        u.setPassword("x");
        u.setLocation("here");

        assertThat(u.getBalance()).isEqualTo(0.0);

        Item it = new Item();
        it.setName("Boat");
        u.addItem(it);

        assertThat(u.getItems()).contains(it);
        assertThat(it.getOwner()).isSameAs(u);

        u.removeItem(it);
        assertThat(u.getItems()).doesNotContain(it);
        assertThat(it.getOwner()).isNull();
    }

    @Test
    void getOwnedBookings_aggregatesBookingsFromItems() {
        User u = new User();
        u.setUsername("bob");
        u.setEmail("b@c.com");
        u.setPassword("p");
        u.setLocation("loc");

        Item it = new Item();
        Booking b = new Booking();
        b.setStartDate(LocalDateTime.now());
        b.setEndDate(LocalDateTime.now().plusDays(1));

        it.setBookings(new java.util.ArrayList<>());
        it.getBookings().add(b);
        u.getItems().add(it);

        assertThat(u.getOwnedBookings()).contains(b);
    }
}
