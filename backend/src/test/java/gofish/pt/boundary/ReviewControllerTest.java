package gofish.pt.boundary;

import app.getxray.xray.junit.customjunitxml.annotations.Requirement;
import com.fasterxml.jackson.databind.ObjectMapper;
import gofish.pt.dto.ReviewDeleteDTO;
import gofish.pt.dto.ReviewRequestDTO;
import gofish.pt.dto.ReviewResponseDTO;
import gofish.pt.dto.ReviewUpdateDTO;
import gofish.pt.entity.Item;
import gofish.pt.entity.Review;
import gofish.pt.entity.User;
import gofish.pt.mapper.ReviewMapper;
import gofish.pt.security.TestSecurityContextHelper;
import gofish.pt.service.ReviewService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReviewController.class)
@AutoConfigureMockMvc(addFilters = false)  // Disable security filters for unit tests
@ActiveProfiles("test")  // Activate test profile to exclude production security config
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReviewService reviewService;

    @MockitoBean
    private ReviewMapper reviewMapper;

    private Review testReview;
    private ReviewResponseDTO testResponseDTO;
    private User testUser;
    private Item testItem;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        testItem = new Item();
        testItem.setId(1L);
        testItem.setName("Test Item");

        testReview = new Review();
        testReview.setId(1L);
        testReview.setRating(5);
        testReview.setComment("Great product!");
        testReview.setCreatedAt(LocalDateTime.now());
        testReview.setUser(testUser);
        testReview.setItem(testItem);

        testResponseDTO = new ReviewResponseDTO();
        testResponseDTO.setId(1L);
        testResponseDTO.setRating(5);
        testResponseDTO.setComment("Great product!");
        testResponseDTO.setUserId(1L);
        testResponseDTO.setUsername("testuser");
        testResponseDTO.setItemId(1L);
        testResponseDTO.setItemName("Test Item");
    }

    @AfterEach
    void tearDown() {
        TestSecurityContextHelper.clearContext();
    }

    @Test
    @DisplayName("POST /api/reviews - Should create review and return 201")
    @Requirement("GF-64")
    void createReview_withValidRequest_returnsCreated() throws Exception {
        TestSecurityContextHelper.setAuthenticatedUser(1L);

        ReviewRequestDTO request = new ReviewRequestDTO(1L, 5, "Great product!");

        when(reviewService.createReview(1L, 1L, 5, "Great product!")).thenReturn(testReview);
        when(reviewMapper.toDTO(testReview)).thenReturn(testResponseDTO);

        mockMvc.perform(post("/api/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.comment").value("Great product!"));

        verify(reviewService).createReview(1L, 1L, 5, "Great product!");
        verify(reviewMapper).toDTO(testReview);
    }

    @Test
    @DisplayName("POST /api/reviews - Should return 400 for invalid rating")
    @Requirement("GF-64")
    void createReview_withInvalidRating_returnsBadRequest() throws Exception {
        TestSecurityContextHelper.setAuthenticatedUser(1L);

        ReviewRequestDTO request = new ReviewRequestDTO(1L, 10, "Invalid rating");

        mockMvc.perform(post("/api/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(reviewService, never()).createReview(any(), any(), any(), any());
    }

    @Test
    @DisplayName("GET /api/reviews/{id} - Should return review when found")
    @Requirement("GF-68")
    void getReview_whenExists_returnsReview() throws Exception {
        when(reviewService.getReview(1L)).thenReturn(Optional.of(testReview));
        when(reviewMapper.toDTO(testReview)).thenReturn(testResponseDTO);

        mockMvc.perform(get("/api/reviews/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.rating").value(5));

        verify(reviewService).getReview(1L);
    }

    @Test
    @DisplayName("GET /api/reviews/{id} - Should return 404 when not found")
    @Requirement("GF-68")
    void getReview_whenNotExists_returns404() throws Exception {
        when(reviewService.getReview(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/reviews/{id}", 999))
                .andExpect(status().isNotFound());

        verify(reviewService).getReview(999L);
    }

    @Test
    @DisplayName("GET /api/reviews/item/{itemId} - Should return paginated reviews")
    @Requirement("GF-68")
    void getReviewsByItem_returnsPaginatedReviews() throws Exception {
        Page<Review> reviewPage = new PageImpl<>(List.of(testReview));

        when(reviewService.getReviewsByItem(eq(1L), any(Pageable.class))).thenReturn(reviewPage);
        when(reviewMapper.toDTO(testReview)).thenReturn(testResponseDTO);

        mockMvc.perform(get("/api/reviews/item/{itemId}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].rating").value(5));

        verify(reviewService).getReviewsByItem(eq(1L), any(Pageable.class));
    }

    @Test
    @DisplayName("GET /api/reviews/user/{userId} - Should return paginated reviews")
    @Requirement("GF-68")
    void getReviewsByUser_returnsPaginatedReviews() throws Exception {
        Page<Review> reviewPage = new PageImpl<>(List.of(testReview));

        when(reviewService.getReviewsByUser(eq(1L), any(Pageable.class))).thenReturn(reviewPage);
        when(reviewMapper.toDTO(testReview)).thenReturn(testResponseDTO);

        mockMvc.perform(get("/api/reviews/user/{userId}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].userId").value(1));

        verify(reviewService).getReviewsByUser(eq(1L), any(Pageable.class));
    }

    @Test
    @DisplayName("GET /api/reviews/item/{itemId}/rating - Should return average rating")
    @Requirement("GF-68")
    void getAverageRating_returnsAverage() throws Exception {
        when(reviewService.getAverageRating(1L)).thenReturn(4.5);

        mockMvc.perform(get("/api/reviews/item/{itemId}/rating", 1))
                .andExpect(status().isOk())
                .andExpect(content().string("4.5"));

        verify(reviewService).getAverageRating(1L);
    }

    @Test
    @DisplayName("GET /api/reviews/item/{itemId}/rating - Should return 0 when no reviews")
    @Requirement("GF-68")
    void getAverageRating_whenNoReviews_returnsZero() throws Exception {
        when(reviewService.getAverageRating(1L)).thenReturn(null);

        mockMvc.perform(get("/api/reviews/item/{itemId}/rating", 1))
                .andExpect(status().isOk())
                .andExpect(content().string("0.0"));

        verify(reviewService).getAverageRating(1L);
    }

    @Test
    @DisplayName("PUT /api/reviews/{id} - Should update review")
    @Requirement("GF-64")
    void updateReview_withValidRequest_returnsUpdatedReview() throws Exception {
        TestSecurityContextHelper.setAuthenticatedUser(1L);

        ReviewUpdateDTO request = new ReviewUpdateDTO(4, "Updated comment");

        testReview.setRating(4);
        testReview.setComment("Updated comment");
        testResponseDTO.setRating(4);
        testResponseDTO.setComment("Updated comment");

        when(reviewService.updateReview(1L, 1L, 4, "Updated comment")).thenReturn(testReview);
        when(reviewMapper.toDTO(testReview)).thenReturn(testResponseDTO);

        mockMvc.perform(put("/api/reviews/{id}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(4))
                .andExpect(jsonPath("$.comment").value("Updated comment"));

        verify(reviewService).updateReview(1L, 1L, 4, "Updated comment");
    }

    @Test
    @DisplayName("DELETE /api/reviews/{id} - Should delete review and return 204")
    @Requirement("GF-64")
    void deleteReview_returnsNoContent() throws Exception {
        TestSecurityContextHelper.setAuthenticatedUser(1L);

        doNothing().when(reviewService).deleteReview(1L, 1L);

        mockMvc.perform(delete("/api/reviews/{id}", 1))
                .andExpect(status().isNoContent());

        verify(reviewService).deleteReview(1L, 1L);
    }
}
