package gofish.pt.boundary;

import com.fasterxml.jackson.databind.ObjectMapper;
import gofish.pt.dto.ConfirmPaymentDTO;
import gofish.pt.dto.CreatePaymentIntentDTO;
import gofish.pt.dto.PaymentIntentResponseDTO;
import gofish.pt.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PaymentService paymentService;

    private CreatePaymentIntentDTO createPaymentIntentDTO;
    private ConfirmPaymentDTO confirmPaymentDTO;
    private PaymentIntentResponseDTO paymentIntentResponseDTO;

    @BeforeEach
    void setUp() {
        createPaymentIntentDTO = new CreatePaymentIntentDTO(1L, 5000L, "eur");
        confirmPaymentDTO = new ConfirmPaymentDTO("pi_test_123", 1L);
        paymentIntentResponseDTO = new PaymentIntentResponseDTO(
                "secret_test_123",
                "pi_test_123",
                5000L,
                "eur",
                "requires_payment_method");
    }

    // --- Create PaymentIntent Tests ---

    @Test
    @DisplayName("POST /api/payments/create-intent - Should create PaymentIntent successfully")
    void createPaymentIntent_WithValidRequest_ShouldReturn200() throws Exception {
        when(paymentService.createPaymentIntent(any(CreatePaymentIntentDTO.class)))
                .thenReturn(paymentIntentResponseDTO);

        mockMvc.perform(post("/api/payments/create-intent")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createPaymentIntentDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientSecret").value("secret_test_123"))
                .andExpect(jsonPath("$.paymentIntentId").value("pi_test_123"))
                .andExpect(jsonPath("$.amount").value(5000))
                .andExpect(jsonPath("$.currency").value("eur"))
                .andExpect(jsonPath("$.status").value("requires_payment_method"));

        verify(paymentService).createPaymentIntent(any(CreatePaymentIntentDTO.class));
    }

    @Test
    @DisplayName("POST /api/payments/create-intent - Should return 404 when booking not found")
    void createPaymentIntent_WhenBookingNotFound_ShouldReturn404() throws Exception {
        when(paymentService.createPaymentIntent(any(CreatePaymentIntentDTO.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));

        mockMvc.perform(post("/api/payments/create-intent")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createPaymentIntentDTO)))
                .andExpect(status().isNotFound());

        verify(paymentService).createPaymentIntent(any(CreatePaymentIntentDTO.class));
    }

    @Test
    @DisplayName("POST /api/payments/create-intent - Should return 400 for non-pending booking")
    void createPaymentIntent_WhenBookingNotPending_ShouldReturn400() throws Exception {
        when(paymentService.createPaymentIntent(any(CreatePaymentIntentDTO.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Cannot process payment for non-pending booking"));

        mockMvc.perform(post("/api/payments/create-intent")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createPaymentIntentDTO)))
                .andExpect(status().isBadRequest());

        verify(paymentService).createPaymentIntent(any(CreatePaymentIntentDTO.class));
    }

    @Test
    @DisplayName("POST /api/payments/create-intent - Should return 400 when bookingId is null")
    void createPaymentIntent_WhenBookingIdNull_ShouldReturn400() throws Exception {
        CreatePaymentIntentDTO invalidDto = new CreatePaymentIntentDTO(null, 5000L, "eur");

        mockMvc.perform(post("/api/payments/create-intent")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(paymentService, never()).createPaymentIntent(any());
    }

    @Test
    @DisplayName("POST /api/payments/create-intent - Should return 400 when amount is null")
    void createPaymentIntent_WhenAmountNull_ShouldReturn400() throws Exception {
        CreatePaymentIntentDTO invalidDto = new CreatePaymentIntentDTO(1L, null, "eur");

        mockMvc.perform(post("/api/payments/create-intent")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(paymentService, never()).createPaymentIntent(any());
    }

    @Test
    @DisplayName("POST /api/payments/create-intent - Should return 400 when amount is negative")
    void createPaymentIntent_WhenAmountNegative_ShouldReturn400() throws Exception {
        CreatePaymentIntentDTO invalidDto = new CreatePaymentIntentDTO(1L, -5000L, "eur");

        mockMvc.perform(post("/api/payments/create-intent")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(paymentService, never()).createPaymentIntent(any());
    }

    // --- Confirm Payment Tests ---

    @Test
    @DisplayName("POST /api/payments/confirm - Should confirm payment successfully")
    void confirmPayment_WithValidRequest_ShouldReturn200() throws Exception {
        PaymentIntentResponseDTO succeededResponse = new PaymentIntentResponseDTO(
                "secret_test_123",
                "pi_test_123",
                5000L,
                "eur",
                "succeeded");
        when(paymentService.confirmPayment(any(ConfirmPaymentDTO.class)))
                .thenReturn(succeededResponse);

        mockMvc.perform(post("/api/payments/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(confirmPaymentDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("succeeded"))
                .andExpect(jsonPath("$.paymentIntentId").value("pi_test_123"));

        verify(paymentService).confirmPayment(any(ConfirmPaymentDTO.class));
    }

    @Test
    @DisplayName("POST /api/payments/confirm - Should return 404 when payment not found")
    void confirmPayment_WhenPaymentNotFound_ShouldReturn404() throws Exception {
        when(paymentService.confirmPayment(any(ConfirmPaymentDTO.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));

        mockMvc.perform(post("/api/payments/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(confirmPaymentDTO)))
                .andExpect(status().isNotFound());

        verify(paymentService).confirmPayment(any(ConfirmPaymentDTO.class));
    }

    @Test
    @DisplayName("POST /api/payments/confirm - Should return 402 when payment failed")
    void confirmPayment_WhenPaymentFailed_ShouldReturn402() throws Exception {
        when(paymentService.confirmPayment(any(ConfirmPaymentDTO.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED,
                        "Payment failed or was cancelled"));

        mockMvc.perform(post("/api/payments/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(confirmPaymentDTO)))
                .andExpect(status().isPaymentRequired());

        verify(paymentService).confirmPayment(any(ConfirmPaymentDTO.class));
    }

    @Test
    @DisplayName("POST /api/payments/confirm - Should return 400 for Stripe processing error")
    void confirmPayment_WhenStripeError_ShouldReturn400() throws Exception {
        when(paymentService.confirmPayment(any(ConfirmPaymentDTO.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Payment confirmation error: Stripe API error"));

        mockMvc.perform(post("/api/payments/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(confirmPaymentDTO)))
                .andExpect(status().isBadRequest());

        verify(paymentService).confirmPayment(any(ConfirmPaymentDTO.class));
    }
}
