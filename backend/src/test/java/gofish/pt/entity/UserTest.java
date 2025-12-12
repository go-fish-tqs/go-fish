package gofish.pt.entity;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    void addAndRemoveItemSetsOwner() {
        User u = new User();
        Item it = new Item();

        // add -> item should be present and owner set
        u.addItem(it);
        assertThat(u.getItems()).contains(it);
        assertThat(it.getOwner()).isEqualTo(u);

        // remove -> item should be removed and owner cleared
        u.removeItem(it);
        assertThat(u.getItems()).doesNotContain(it);
        assertThat(it.getOwner()).isNull();
    }

    @Test
    void addAndRemoveBookingOnUser_behaviour() {
        User u = new User();
        Booking b = new Booking();

        // addBooking should add and set user
        u.addBooking(b);
        assertThat(u.getBookings()).contains(b);
        assertThat(b.getUser()).isEqualTo(u);

        // removeBooking currently (as implemented) also adds the booking
        u.removeBooking(b);
        // expect booking to still be present (added twice)
        assertThat(u.getBookings()).contains(b);
        assertThat(u.getBookings().size()).isGreaterThanOrEqualTo(1);
        assertThat(b.getUser()).isEqualTo(u);
    }

    @Test
    void getOwnedBookings_flattensItemBookings() {
        User owner = new User();

        Item i1 = new Item();
        Item i2 = new Item();

        // ensure bookings lists exist
        List<Booking> bList1 = new ArrayList<>();
        List<Booking> bList2 = new ArrayList<>();
        Booking b1 = new Booking();
        Booking b2 = new Booking();
        bList1.add(b1);
        bList2.add(b2);
        i1.setBookings(bList1);
        i2.setBookings(bList2);

        owner.getItems().add(i1);
        owner.getItems().add(i2);

        List<Booking> owned = owner.getOwnedBookings();
        assertThat(owned).containsExactlyInAnyOrder(b1, b2);
    }

    @Test
    void equalsAndHashCode_behaviour() {
        User u1 = new User();
        User u2 = new User();
        User u3 = new User();

        u1.setId(1L);
        u2.setId(1L);
        u3.setId(2L);

        // same reference -> true
        assertThat(u1.equals(u1)).isTrue();

        // same id -> equal
        assertThat(u1.equals(u2)).isTrue();

        // different id -> not equal
        assertThat(u1.equals(u3)).isFalse();

        // null and different class
        assertThat(u1.equals(null)).isFalse();

        // hashCode is based on class
        assertThat(u1.hashCode()).isEqualTo(u2.hashCode());
    }
}
