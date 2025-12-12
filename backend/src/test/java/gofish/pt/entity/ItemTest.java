package gofish.pt.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ItemTest {

    @Test
    void defaultAvailable_isTrue_and_fieldsWork() {
        Item it = new Item();
        it.setName("Rod");
        it.setDescription("Nice rod");
        it.setPrice(15.0);
        it.setMaterial(Material.CARBON_FIBER);
        it.setCategory(Category.RODS);

        assertThat(it.getAvailable()).isTrue();
        assertThat(it.getName()).isEqualTo("Rod");
        assertThat(it.getDescription()).isEqualTo("Nice rod");
        assertThat(it.getPrice()).isEqualTo(15.0);
    }

    @Test
    void addBooking_setsBidirectionalLink_andRemoveClearsIt() {
        Item it = new Item();
        List<Booking> list = new ArrayList<>();
        it.setBookings(list);

        Booking b = new Booking();
        b.setStartDate(LocalDateTime.now());
        b.setEndDate(LocalDateTime.now().plusDays(1));

        it.addBooking(b);

        assertThat(it.getBookings()).contains(b);
        assertThat(b.getItem()).isSameAs(it);

        it.removeBooking(b);

        assertThat(it.getBookings()).doesNotContain(b);
        assertThat(b.getItem()).isNull();
    }

    @Test
    void photoUrls_list_canBeSetAndRetrieved() {
        Item it = new Item();
        List<String> photos = new ArrayList<>();
        photos.add("a.jpg");
        it.setPhotoUrls(photos);

        assertThat(it.getPhotoUrls()).hasSize(1).containsExactly("a.jpg");
    }
}
