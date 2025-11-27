package gofish.pt.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ItemTest {

    @Test
    void testAvailableByDefault() {
        Item i = new Item();
        assertThat(i.getAvailable()).isTrue();
    }
}
