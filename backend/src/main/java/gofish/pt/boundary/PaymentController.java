package gofish.pt.boundary;

import gofish.pt.dto.ConfirmPaymentDTO;
import gofish.pt.dto.CreatePaymentIntentDTO;
import gofish.pt.dto.PaymentIntentResponseDTO;
import gofish.pt.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Create a Stripe PaymentIntent for a booking
     */
    @PostMapping("/create-intent")
    public ResponseEntity<PaymentIntentResponseDTO> createPaymentIntent(
            @Valid @RequestBody CreatePaymentIntentDTO request) {

        PaymentIntentResponseDTO response = paymentService.createPaymentIntent(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Confirm a payment after frontend processing
     */
    @PostMapping("/confirm")
    public ResponseEntity<PaymentIntentResponseDTO> confirmPayment(
            @RequestBody ConfirmPaymentDTO request) {

        PaymentIntentResponseDTO response = paymentService.confirmPayment(request);
        return ResponseEntity.ok(response);
    }
}
