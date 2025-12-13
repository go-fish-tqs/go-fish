package gofish.pt.service;

import gofish.pt.entity.Review;
import gofish.pt.repository.ItemRepository;
import gofish.pt.repository.ReviewRepository;
import gofish.pt.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    // --- GET methods ---

    public Optional<Review> getReview(Long id) {
        return reviewRepository.findById(id);
    }

    public Page<Review> getReviewsByItem(Long itemId, Pageable pageable) {
        return reviewRepository.findByItemId(itemId, pageable);
    }

    public Page<Review> getReviewsByUser(Long userId, Pageable pageable) {
        return reviewRepository.findByUserId(userId, pageable);
    }

    public Double getAverageRating(Long itemId) {
        return reviewRepository.calculateAverageRating(itemId);
    }

    // --- CREATE method ---

    public Review createReview(Long userId, Long itemId, Integer rating, String comment) {
        // 1. Validate user exists
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // 2. Validate item exists
        var item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));

        // 3. Check for duplicate review (one review per user per item)
        if (reviewRepository.existsByUserIdAndItemId(userId, itemId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You have already reviewed this item");
        }

        // 4. Validate rating range (additional check, entity validation should catch it
        // too)
        if (rating < 1 || rating > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rating must be between 1 and 5");
        }

        // 5. Create and save review
        Review review = new Review();
        review.setUser(user);
        review.setItem(item);
        review.setRating(rating);
        review.setComment(comment);

        return reviewRepository.save(review);
    }

    // --- UPDATE method ---

    public Review updateReview(Long reviewId, Long userId, Integer rating, String comment) {
        // 1. Find review
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));

        // 2. Security check: only the author can update their review
        if (!review.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only edit your own reviews");
        }

        // 3. Validate rating range
        if (rating < 1 || rating > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rating must be between 1 and 5");
        }

        // 4. Update fields
        review.setRating(rating);
        review.setComment(comment);

        return reviewRepository.save(review);
    }

    // --- DELETE method ---

    public void deleteReview(Long reviewId, Long userId) {
        // 1. Find review
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));

        // 2. Security check: only the author can delete their review
        if (!review.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only delete your own reviews");
        }

        // 3. Delete
        reviewRepository.delete(review);
    }
}
