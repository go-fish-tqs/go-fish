package gofish.pt.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import gofish.pt.config.TestSecurityConfig;
import gofish.pt.dto.ReviewDeleteDTO;
import gofish.pt.dto.ReviewRequestDTO;
import gofish.pt.dto.ReviewUpdateDTO;
import gofish.pt.entity.*;
import gofish.pt.repository.ItemRepository;
import gofish.pt.repository.ReviewRepository;
import gofish.pt.repository.UserRepository;
import gofish.pt.security.TestSecurityContextHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@Transactional
class ReviewControllerIT {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;

    private User reviewer;
    private User otherUser;
    private Item fishingRod;

    @BeforeEach
    void setUp() {
        reviewRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();

        // Create reviewer
        reviewer = new User();
        reviewer.setUsername("reviewer_joao");
        reviewer.setEmail("joao@fish.pt");
        reviewer.setPassword("pass123");
        reviewer.setLocation("Lisboa");
        reviewer.setBalance(0.0);
        reviewer = userRepository.save(reviewer);

        // Create another user
        otherUser = new User();
        otherUser.setUsername("other_maria");
        otherUser.setEmail("maria@fish.pt");
        otherUser.setPassword("pass456");
        otherUser.setLocation("Porto");
        otherUser.setBalance(0.0);
        otherUser = userRepository.save(otherUser);

        // Create item
        fishingRod = new Item();
        fishingRod.setName("Best Fishing Rod");
        fishingRod.setDescription("A fantastic fishing rod");
        fishingRod.setPrice(45.0);
        fishingRod.setCategory(Category.RODS);
        fishingRod.setMaterial(Material.CARBON_FIBER);
        fishingRod.setOwner(otherUser); // Owned by another user
        fishingRod.setAvailable(true);
        fishingRod = itemRepository.save(fishingRod);
    }

    @AfterEach
    void tearDown() {
        TestSecurityContextHelper.clearContext();
    }

    @Test
    @DisplayName("POST /api/reviews - Should create review with status 201")
    void createReview() throws Exception {
        TestSecurityContextHelper.setAuthenticatedUser(reviewer.getId());

        ReviewRequestDTO request = new ReviewRequestDTO();
        request.setItemId(fishingRod.getId());
        request.setRating(5);
        request.setComment("Amazing fishing rod!");

        mockMvc.perform(post("/api/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.comment").value("Amazing fishing rod!"))
                .andExpect(jsonPath("$.username").value("reviewer_joao"))
                .andExpect(jsonPath("$.itemName").value("Best Fishing Rod"));
    }

    @Test
    @DisplayName("POST /api/reviews - Should return 409 for duplicate review")
    void createDuplicateReview() throws Exception {
        // Create first review directly
        Review existing = new Review();
        existing.setUser(reviewer);
        existing.setItem(fishingRod);
        existing.setRating(4);
        existing.setComment("Good rod");
        reviewRepository.save(existing);

        // Try to create another
        TestSecurityContextHelper.setAuthenticatedUser(reviewer.getId());

        ReviewRequestDTO request = new ReviewRequestDTO();
        request.setItemId(fishingRod.getId());
        request.setRating(5);
        request.setComment("Changed my mind!");

        mockMvc.perform(post("/api/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /api/reviews - Should return 400 for invalid rating")
    void createReview_InvalidRating() throws Exception {
        TestSecurityContextHelper.setAuthenticatedUser(reviewer.getId());

        ReviewRequestDTO request = new ReviewRequestDTO();
        request.setItemId(fishingRod.getId());
        request.setRating(10); // Invalid - above 5
        request.setComment("Too high score");

        mockMvc.perform(post("/api/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/reviews/{id} - Should return review")
    void getReview() throws Exception {
        Review review = new Review();
        review.setUser(reviewer);
        review.setItem(fishingRod);
        review.setRating(4);
        review.setComment("Pretty good");
        review = reviewRepository.save(review);

        mockMvc.perform(get("/api/reviews/{id}", review.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(4))
                .andExpect(jsonPath("$.comment").value("Pretty good"));
    }

    @Test
    @DisplayName("GET /api/reviews/{id} - Should return 404 if not found")
    void getReviewNotFound() throws Exception {
        mockMvc.perform(get("/api/reviews/{id}", 9999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/reviews/item/{itemId} - Should return paginated reviews")
    void getReviewsByItem() throws Exception {
        // Create multiple reviews
        Review r1 = new Review();
        r1.setUser(reviewer);
        r1.setItem(fishingRod);
        r1.setRating(5);
        r1.setComment("Excellent!");
        reviewRepository.save(r1);

        Review r2 = new Review();
        r2.setUser(otherUser);
        r2.setItem(fishingRod);
        r2.setRating(4);
        r2.setComment("Good product");
        reviewRepository.save(r2);

        mockMvc.perform(get("/api/reviews/item/{itemId}", fishingRod.getId())
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    @DisplayName("GET /api/reviews/user/{userId} - Should return user's reviews")
    void getReviewsByUser() throws Exception {
        Review review = new Review();
        review.setUser(reviewer);
        review.setItem(fishingRod);
        review.setRating(5);
        review.setComment("My review");
        reviewRepository.save(review);

        mockMvc.perform(get("/api/reviews/user/{userId}", reviewer.getId())
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].username").value("reviewer_joao"));
    }

    @Test
    @DisplayName("GET /api/reviews/item/{itemId}/rating - Should return average rating")
    void getAverageRating() throws Exception {
        // Create reviews with different ratings
        Review r1 = new Review();
        r1.setUser(reviewer);
        r1.setItem(fishingRod);
        r1.setRating(4);
        reviewRepository.save(r1);

        Review r2 = new Review();
        r2.setUser(otherUser);
        r2.setItem(fishingRod);
        r2.setRating(5);
        reviewRepository.save(r2);

        mockMvc.perform(get("/api/reviews/item/{itemId}/rating", fishingRod.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(4.5));
    }

    @Test
    @DisplayName("PUT /api/reviews/{id} - Author should update review")
    void updateReview() throws Exception {
        Review review = new Review();
        review.setUser(reviewer);
        review.setItem(fishingRod);
        review.setRating(3);
        review.setComment("Initial review");
        review = reviewRepository.save(review);

        TestSecurityContextHelper.setAuthenticatedUser(reviewer.getId());

        ReviewUpdateDTO updateDTO = new ReviewUpdateDTO();
        updateDTO.setRating(5);
        updateDTO.setComment("Updated - much better now!");

        mockMvc.perform(put("/api/reviews/{id}", review.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.comment").value("Updated - much better now!"));
    }

    @Test
    @DisplayName("PUT /api/reviews/{id} - Should return 403 when non-author tries to update")
    void updateReview_NonAuthor() throws Exception {
        Review review = new Review();
        review.setUser(reviewer);
        review.setItem(fishingRod);
        review.setRating(3);
        review.setComment("Initial review");
        review = reviewRepository.save(review);

        TestSecurityContextHelper.setAuthenticatedUser(otherUser.getId()); // Wrong user!

        ReviewUpdateDTO updateDTO = new ReviewUpdateDTO();
        updateDTO.setRating(1);
        updateDTO.setComment("Trying to sabotage");

        mockMvc.perform(put("/api/reviews/{id}", review.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/reviews/{id} - Author should delete review")
    void deleteReview() throws Exception {
        Review review = new Review();
        review.setUser(reviewer);
        review.setItem(fishingRod);
        review.setRating(5);
        review.setComment("To be deleted");
        review = reviewRepository.save(review);

        TestSecurityContextHelper.setAuthenticatedUser(reviewer.getId());

        mockMvc.perform(delete("/api/reviews/{id}", review.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/reviews/{id} - Should return 403 when non-author tries to delete")
    void deleteReview_NonAuthor() throws Exception {
        Review review = new Review();
        review.setUser(reviewer);
        review.setItem(fishingRod);
        review.setRating(5);
        review.setComment("Cannot be deleted by others");
        review = reviewRepository.save(review);

        TestSecurityContextHelper.setAuthenticatedUser(otherUser.getId()); // Wrong user!

        mockMvc.perform(delete("/api/reviews/{id}", review.getId()))
                .andExpect(status().isForbidden());
    }
}
