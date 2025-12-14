package gofish.pt.service;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import gofish.pt.dto.ConfirmPaymentDTO;
import gofish.pt.dto.CreatePaymentIntentDTO;
import gofish.pt.dto.PaymentIntentResponseDTO;
import gofish.pt.entity.Booking;
import gofish.pt.entity.BookingStatus;
import gofish.pt.entity.Payment;
import gofish.pt.entity.PaymentStatus;
import gofish.pt.repository.BookingRepository;
import gofish.pt.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;

    /**
     * Creates a Stripe PaymentIntent for a booking
     */
    public PaymentIntentResponseDTO createPaymentIntent(CreatePaymentIntentDTO dto) {
        // Validate booking exists
        Booking booking = bookingRepository.findById(dto.getBookingId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));

        // Check booking is in PENDING status
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot process payment for non-pending booking");
        }

        try {
            // Create Stripe PaymentIntent
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(dto.getAmount())
                    .setCurrency(dto.getCurrency() != null ? dto.getCurrency() : "eur")
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .build())
                    .putMetadata("booking_id", booking.getId().toString())
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);

            // Save payment record
            Payment payment = new Payment();
            payment.setBooking(booking);
            payment.setStripePaymentIntentId(paymentIntent.getId());
            payment.setAmount(dto.getAmount());
            payment.setCurrency(dto.getCurrency() != null ? dto.getCurrency() : "eur");
            payment.setStatus(PaymentStatus.PENDING);
            paymentRepository.save(payment);

            log.info("Created PaymentIntent {} for booking {}", paymentIntent.getId(), booking.getId());

            return new PaymentIntentResponseDTO(
                    paymentIntent.getClientSecret(),
                    paymentIntent.getId(),
                    dto.getAmount(),
                    dto.getCurrency(),
                    paymentIntent.getStatus());

        } catch (StripeException e) {
            log.error("Stripe error creating PaymentIntent: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment processing error: " + e.getMessage());
        }
    }

    /**
     * Confirms a payment and updates booking status
     */
    public PaymentIntentResponseDTO confirmPayment(ConfirmPaymentDTO dto) {
        // Find payment record
        Payment payment = paymentRepository.findByStripePaymentIntentId(dto.getPaymentIntentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));

        try {
            // Retrieve PaymentIntent from Stripe to check status
            PaymentIntent paymentIntent = PaymentIntent.retrieve(dto.getPaymentIntentId());
            String status = paymentIntent.getStatus();

            log.info("PaymentIntent {} status: {}", dto.getPaymentIntentId(), status);

            if ("succeeded".equals(status)) {
                // Update payment status
                payment.setStatus(PaymentStatus.SUCCEEDED);
                paymentRepository.save(payment);

                // Update booking status to CONFIRMED
                Booking booking = payment.getBooking();
                booking.setStatus(BookingStatus.CONFIRMED);
                bookingRepository.save(booking);

                log.info("Payment {} succeeded, booking {} confirmed", dto.getPaymentIntentId(), booking.getId());
            } else if ("requires_payment_method".equals(status) || "canceled".equals(status)) {
                payment.setStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);

                throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED, "Payment failed or was cancelled");
            }

            return new PaymentIntentResponseDTO(
                    paymentIntent.getClientSecret(),
                    paymentIntent.getId(),
                    payment.getAmount(),
                    payment.getCurrency(),
                    status);

        } catch (StripeException e) {
            log.error("Stripe error confirming payment: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment confirmation error: " + e.getMessage());
        }
    }

    /**
     * Calculate total price for a booking based on item daily rate and duration
     */
    public Long calculateBookingAmount(Booking booking) {
        if (booking.getItem().getPrice() == null) {
            return 0L;
        }

        long days = java.time.temporal.ChronoUnit.DAYS.between(
                booking.getStartDate(),
                booking.getEndDate());

        if (days < 1)
            days = 1; // Minimum 1 day

        // Convert price to cents and multiply by days
        return Math.round(booking.getItem().getPrice() * 100 * days);
    }
}
