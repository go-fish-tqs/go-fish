package gofish.pt.service;

import app.getxray.xray.junit.customjunitxml.annotations.Requirement;
import gofish.pt.entity.*;
import gofish.pt.repository.ItemRepository;
import gofish.pt.repository.ReviewRepository;
import gofish.pt.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ReviewService reviewService;

    private User reviewer;
    private User otherUser;
    private Item fishingRod;
    private Review review;

    @BeforeEach
    void setup() {
        reviewer = new User();
        reviewer.setId(1L);
        reviewer.setUsername("john_fisher");

        otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername("jane_fisher");

        fishingRod = new Item();
        fishingRod.setId(10L);
        fishingRod.setName("Pro Fishing Rod");

        review = new Review();
        review.setId(100L);
        review.setUser(reviewer);
        review.setItem(fishingRod);
        review.setRating(5);
        review.setComment("Excellent rod!");
        review.setCreatedAt(LocalDateTime.now());
    }

    // --- CREATE TESTS ---

    @Test
    @DisplayName("Should create review successfully")
    @Requirement("GF-64")
    void shouldCreateReview_WhenValid() {
        when(userRepository.findById(reviewer.getId())).thenReturn(Optional.of(reviewer));
        when(itemRepository.findById(fishingRod.getId())).thenReturn(Optional.of(fishingRod));
        when(reviewRepository.existsByUserIdAndItemId(reviewer.getId(), fishingRod.getId())).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenAnswer(i -> i.getArguments()[0]);

        Review created = reviewService.createReview(reviewer.getId(), fishingRod.getId(), 5, "Great product!");

        assertThat(created).isNotNull();
        assertThat(created.getRating()).isEqualTo(5);
        assertThat(created.getUser()).isEqualTo(reviewer);
        assertThat(created.getItem()).isEqualTo(fishingRod);

        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    @DisplayName("Should throw error when creating duplicate review")
    @Requirement("GF-64")
    void shouldThrowError_WhenDuplicateReview() {
        when(userRepository.findById(reviewer.getId())).thenReturn(Optional.of(reviewer));
        when(itemRepository.findById(fishingRod.getId())).thenReturn(Optional.of(fishingRod));
        when(reviewRepository.existsByUserIdAndItemId(reviewer.getId(), fishingRod.getId())).thenReturn(true);

        assertThatThrownBy(() -> reviewService.createReview(reviewer.getId(), fishingRod.getId(), 5, "Another review"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("already reviewed");

        verify(reviewRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw error when rating is out of range")
    @Requirement("GF-64")
    void shouldThrowError_WhenRatingOutOfRange() {
        when(userRepository.findById(reviewer.getId())).thenReturn(Optional.of(reviewer));
        when(itemRepository.findById(fishingRod.getId())).thenReturn(Optional.of(fishingRod));
        when(reviewRepository.existsByUserIdAndItemId(reviewer.getId(), fishingRod.getId())).thenReturn(false);

        assertThatThrownBy(() -> reviewService.createReview(reviewer.getId(), fishingRod.getId(), 6, "Too high rating"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("between 1 and 5");

        verify(reviewRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw error when user not found")
    @Requirement("GF-64")
    void shouldThrowError_WhenUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.createReview(999L, fishingRod.getId(), 5, "Review"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("Should throw error when item not found")
    @Requirement("GF-64")
    void shouldThrowError_WhenItemNotFound() {
        when(userRepository.findById(reviewer.getId())).thenReturn(Optional.of(reviewer));
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.createReview(reviewer.getId(), 999L, 5, "Review"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Item not found");
    }

    // --- UPDATE TESTS ---

    @Test
    @DisplayName("Author should be able to update their review")
    @Requirement("GF-64")
    void authorShouldUpdateReview() {
        when(reviewRepository.findById(review.getId())).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenAnswer(i -> i.getArguments()[0]);

        Review updated = reviewService.updateReview(review.getId(), reviewer.getId(), 4, "Updated comment");

        assertThat(updated.getRating()).isEqualTo(4);
        assertThat(updated.getComment()).isEqualTo("Updated comment");

        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    @DisplayName("Should throw error when non-author tries to update")
    @Requirement("GF-64")
    void shouldThrowError_WhenNonAuthorTriesToUpdate() {

        when(reviewRepository.findById(review.getId())).thenReturn(Optional.of(review));

        assertThatThrownBy(() -> reviewService.updateReview(review.getId(), otherUser.getId(), 4, "Trying to update"))
                .isInstanceOf(ResponseStatusException.class);
    }

    // --- DELETE TESTS ---

    @Test
    @DisplayName("Author should be able to delete their review")
    @Requirement("GF-64")
    void authorShouldDeleteReview() {
        when(reviewRepository.findById(review.getId())).thenReturn(Optional.of(review));
        doNothing().when(reviewRepository).delete(review);

        reviewService.deleteReview(review.getId(), reviewer.getId());

        verify(reviewRepository, times(1)).delete(review);
    }

    @Test
    @DisplayName("Should throw error when non-author tries to delete")
    @Requirement("GF-64")
    void shouldThrowError_WhenNonAuthorTriesToDelete() {
        when(reviewRepository.findById(review.getId())).thenReturn(Optional.of(review));

        assertThatThrownBy(() -> reviewService.deleteReview(review.getId(), otherUser.getId()))
                .isInstanceOf(ResponseStatusException.class);

        verify(reviewRepository, never()).delete(any());
    }

    // --- READ TESTS ---

    @Test
    @DisplayName("Should return paginated reviews by item")
    @Requirement("GF-68")
    void shouldReturnPaginatedReviewsByItem() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Review> expectedPage = new PageImpl<>(List.of(review));

        when(reviewRepository.findByItemId(fishingRod.getId(), pageable)).thenReturn(expectedPage);

        Page<Review> result = reviewService.getReviewsByItem(fishingRod.getId(), pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(review);
    }

    @Test
    @DisplayName("Should return paginated reviews by user")
    @Requirement("GF-68")
    void shouldReturnPaginatedReviewsByUser() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Review> expectedPage = new PageImpl<>(List.of(review));

        when(reviewRepository.findByUserId(reviewer.getId(), pageable)).thenReturn(expectedPage);

        Page<Review> result = reviewService.getReviewsByUser(reviewer.getId(), pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(review);
    }

    @Test
    @DisplayName("Should calculate average rating for item")
    @Requirement("GF-68")
    void shouldCalculateAverageRating() {
        when(reviewRepository.calculateAverageRating(fishingRod.getId())).thenReturn(4.5);

        Double average = reviewService.getAverageRating(fishingRod.getId());

        assertThat(average).isEqualTo(4.5);
    }
}
