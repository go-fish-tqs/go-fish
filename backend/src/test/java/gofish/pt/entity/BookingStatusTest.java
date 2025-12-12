package gofish.pt.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BookingStatusTest {

    @Test
    void bookingStatus_enumContainsExpected() {
        BookingStatus s = BookingStatus.PENDING;
        assertThat(s.name()).isEqualTo("PENDING");
        assertThat(BookingStatus.values()).contains(BookingStatus.ACTIVE, BookingStatus.CANCELLED, BookingStatus.COMPLETED, BookingStatus.CONFIRMED, BookingStatus.PENDING);
    }
}
