package gofish.pt.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ItemAdditionalTest {

    @Test
    void addBooking_whenBookingsNull_throwsNPE() {
        Item it = new Item();
        Booking b = new Booking();
        b.setStartDate(LocalDateTime.now());
        b.setEndDate(LocalDateTime.now().plusDays(1));

        assertThatThrownBy(() -> it.addBooking(b)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void removeBooking_notPresent_doesNotThrow_and_clearsItem() {
        Item it = new Item();
        it.setBookings(new ArrayList<>());

        Booking b = new Booking();
        // not added
        it.removeBooking(b);

        assertThat(it.getBookings()).doesNotContain(b);
        assertThat(b.getItem()).isNull();
    }

    @Test
    void jsonSerialization_includesCategoryString_notSubCategories() throws Exception {
        Item it = new Item();
        it.setId(5L);
        it.setName("Net");
        it.setDescription("Fishing net");
        it.setCategory(Category.NETS);
        it.setMaterial(Material.NYLON);
        it.setPrice(9.5);

        ObjectMapper om = new ObjectMapper();
        String json = om.writeValueAsString(it);

        assertThat(json).contains("NETS");
        assertThat(json).doesNotContain("subCategories");
    }
}
