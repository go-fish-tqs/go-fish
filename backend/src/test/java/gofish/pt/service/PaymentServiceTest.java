package gofish.pt.service;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import gofish.pt.dto.ConfirmPaymentDTO;
import gofish.pt.dto.CreatePaymentIntentDTO;
import gofish.pt.dto.PaymentIntentResponseDTO;
import gofish.pt.entity.*;
import gofish.pt.repository.BookingRepository;
import gofish.pt.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private PaymentService paymentService;

    private User renter;
    private User owner;
    private Item item;
    private Booking booking;
    private Payment payment;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);
        owner.setUsername("owner_user");

        renter = new User();
        renter.setId(2L);
        renter.setUsername("renter_user");

        item = new Item();
        item.setId(1L);
        item.setName("Fishing Rod");
        item.setPrice(25.0);
        item.setOwner(owner);

        booking = new Booking();
        booking.setId(100L);
        booking.setUser(renter);
        booking.setItem(item);
        booking.setStatus(BookingStatus.PENDING);
        booking.setStartDate(LocalDateTime.now().plusDays(1));
        booking.setEndDate(LocalDateTime.now().plusDays(3));

        payment = new Payment();
        payment.setId(1L);
        payment.setBooking(booking);
        payment.setStripePaymentIntentId("pi_test_123");
        payment.setAmount(5000L);
        payment.setCurrency("eur");
        payment.setStatus(PaymentStatus.PENDING);
    }

    // --- createPaymentIntent Tests ---

    @Test
    @DisplayName("Should throw error when booking not found")
    void createPaymentIntent_WhenBookingNotFound_ShouldThrowError() {
        CreatePaymentIntentDTO dto = new CreatePaymentIntentDTO(999L, 5000L, "eur");
        when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.createPaymentIntent(dto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Booking not found");

        verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw error when booking is not in PENDING status")
    void createPaymentIntent_WhenBookingNotPending_ShouldThrowError() {
        booking.setStatus(BookingStatus.CONFIRMED);
        CreatePaymentIntentDTO dto = new CreatePaymentIntentDTO(booking.getId(), 5000L, "eur");
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> paymentService.createPaymentIntent(dto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Cannot process payment for non-pending booking");

        verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should create PaymentIntent successfully for valid pending booking")
    void createPaymentIntent_WithValidPendingBooking_ShouldSucceed() {
        CreatePaymentIntentDTO dto = new CreatePaymentIntentDTO(booking.getId(), 5000L, "eur");
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArguments()[0]);

        try (MockedStatic<PaymentIntent> mockedPaymentIntent = mockStatic(PaymentIntent.class)) {
            PaymentIntent mockIntent = mock(PaymentIntent.class);
            when(mockIntent.getId()).thenReturn("pi_test_123");
            when(mockIntent.getClientSecret()).thenReturn("secret_test");
            when(mockIntent.getStatus()).thenReturn("requires_payment_method");

            mockedPaymentIntent.when(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class)))
                    .thenReturn(mockIntent);

            PaymentIntentResponseDTO response = paymentService.createPaymentIntent(dto);

            assertThat(response).isNotNull();
            assertThat(response.getPaymentIntentId()).isEqualTo("pi_test_123");
            assertThat(response.getClientSecret()).isEqualTo("secret_test");
            assertThat(response.getAmount()).isEqualTo(5000L);
            assertThat(response.getCurrency()).isEqualTo("eur");

            verify(paymentRepository, times(1)).save(any(Payment.class));
        }
    }

    @Test
    @DisplayName("Should use default currency when not provided")
    void createPaymentIntent_WithNullCurrency_ShouldUseEuro() {
        CreatePaymentIntentDTO dto = new CreatePaymentIntentDTO(booking.getId(), 5000L, null);
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment savedPayment = invocation.getArgument(0);
            assertThat(savedPayment.getCurrency()).isEqualTo("eur");
            return savedPayment;
        });

        try (MockedStatic<PaymentIntent> mockedPaymentIntent = mockStatic(PaymentIntent.class)) {
            PaymentIntent mockIntent = mock(PaymentIntent.class);
            when(mockIntent.getId()).thenReturn("pi_test_123");
            when(mockIntent.getClientSecret()).thenReturn("secret_test");
            when(mockIntent.getStatus()).thenReturn("requires_payment_method");

            mockedPaymentIntent.when(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class)))
                    .thenReturn(mockIntent);

            PaymentIntentResponseDTO response = paymentService.createPaymentIntent(dto);

            assertThat(response).isNotNull();
            verify(paymentRepository, times(1)).save(any(Payment.class));
        }
    }

    @Test
    @DisplayName("Should handle Stripe exception gracefully")
    void createPaymentIntent_WhenStripeException_ShouldThrowError() {
        CreatePaymentIntentDTO dto = new CreatePaymentIntentDTO(booking.getId(), 5000L, "eur");
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        try (MockedStatic<PaymentIntent> mockedPaymentIntent = mockStatic(PaymentIntent.class)) {
            mockedPaymentIntent.when(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class)))
                    .thenThrow(new RuntimeException("Stripe API error"));

            assertThatThrownBy(() -> paymentService.createPaymentIntent(dto))
                    .isInstanceOf(RuntimeException.class);

            verify(paymentRepository, never()).save(any());
        }
    }

    // --- confirmPayment Tests ---

    @Test
    @DisplayName("Should throw error when payment not found")
    void confirmPayment_WhenPaymentNotFound_ShouldThrowError() {
        ConfirmPaymentDTO dto = new ConfirmPaymentDTO("pi_unknown", booking.getId());
        when(paymentRepository.findByStripePaymentIntentId("pi_unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.confirmPayment(dto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Payment not found");
    }

    @Test
    @DisplayName("Should confirm payment and update booking when Stripe status is succeeded")
    void confirmPayment_WhenSucceeded_ShouldUpdatePaymentAndBooking() {
        ConfirmPaymentDTO dto = new ConfirmPaymentDTO("pi_test_123", booking.getId());
        when(paymentRepository.findByStripePaymentIntentId("pi_test_123")).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArguments()[0]);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArguments()[0]);

        try (MockedStatic<PaymentIntent> mockedPaymentIntent = mockStatic(PaymentIntent.class)) {
            PaymentIntent mockIntent = mock(PaymentIntent.class);
            when(mockIntent.getId()).thenReturn("pi_test_123");
            when(mockIntent.getClientSecret()).thenReturn("secret_test");
            when(mockIntent.getStatus()).thenReturn("succeeded");

            mockedPaymentIntent.when(() -> PaymentIntent.retrieve("pi_test_123"))
                    .thenReturn(mockIntent);

            PaymentIntentResponseDTO response = paymentService.confirmPayment(dto);

            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo("succeeded");
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCEEDED);
            assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);

            verify(paymentRepository, times(1)).save(payment);
            verify(bookingRepository, times(1)).save(booking);
        }
    }

    @Test
    @DisplayName("Should fail payment when Stripe status requires_payment_method")
    void confirmPayment_WhenRequiresPaymentMethod_ShouldFail() {
        ConfirmPaymentDTO dto = new ConfirmPaymentDTO("pi_test_123", booking.getId());
        when(paymentRepository.findByStripePaymentIntentId("pi_test_123")).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArguments()[0]);

        try (MockedStatic<PaymentIntent> mockedPaymentIntent = mockStatic(PaymentIntent.class)) {
            PaymentIntent mockIntent = mock(PaymentIntent.class);
            when(mockIntent.getStatus()).thenReturn("requires_payment_method");

            mockedPaymentIntent.when(() -> PaymentIntent.retrieve("pi_test_123"))
                    .thenReturn(mockIntent);

            assertThatThrownBy(() -> paymentService.confirmPayment(dto))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Payment failed or was cancelled");

            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
            verify(paymentRepository, times(1)).save(payment);
        }
    }

    @Test
    @DisplayName("Should fail payment when Stripe status is canceled")
    void confirmPayment_WhenCanceled_ShouldFail() {
        ConfirmPaymentDTO dto = new ConfirmPaymentDTO("pi_test_123", booking.getId());
        when(paymentRepository.findByStripePaymentIntentId("pi_test_123")).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArguments()[0]);

        try (MockedStatic<PaymentIntent> mockedPaymentIntent = mockStatic(PaymentIntent.class)) {
            PaymentIntent mockIntent = mock(PaymentIntent.class);
            when(mockIntent.getStatus()).thenReturn("canceled");

            mockedPaymentIntent.when(() -> PaymentIntent.retrieve("pi_test_123"))
                    .thenReturn(mockIntent);

            assertThatThrownBy(() -> paymentService.confirmPayment(dto))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Payment failed or was cancelled");

            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
        }
    }

    // --- calculateBookingAmount Tests ---

    @Test
    @DisplayName("Should calculate booking amount correctly for multiple days")
    void calculateBookingAmount_ForMultipleDays_ShouldCalculateCorrectly() {
        // 3 days booking at 25.0 per day = 75.0 EUR = 7500 cents
        Long amount = paymentService.calculateBookingAmount(booking);

        // Duration from now+1 to now+3 is 2 days
        assertThat(amount).isEqualTo(5000L); // 25 * 100 * 2 = 5000
    }

    @Test
    @DisplayName("Should return minimum 1 day when duration is less than 1 day")
    void calculateBookingAmount_WhenLessThanOneDay_ShouldReturnOneDayPrice() {
        booking.setStartDate(LocalDateTime.now().plusDays(1));
        booking.setEndDate(LocalDateTime.now().plusDays(1).plusHours(12));

        Long amount = paymentService.calculateBookingAmount(booking);

        assertThat(amount).isEqualTo(2500L); // 25 * 100 * 1 = 2500 cents (minimum 1 day)
    }

    @Test
    @DisplayName("Should return 0 when item has no price")
    void calculateBookingAmount_WhenNoPriceSet_ShouldReturnZero() {
        item.setPrice(null);

        Long amount = paymentService.calculateBookingAmount(booking);

        assertThat(amount).isEqualTo(0L);
    }
}
