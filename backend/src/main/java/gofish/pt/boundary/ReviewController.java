package gofish.pt.boundary;

import gofish.pt.dto.ReviewDeleteDTO;
import gofish.pt.dto.ReviewRequestDTO;
import gofish.pt.dto.ReviewResponseDTO;
import gofish.pt.dto.ReviewUpdateDTO;
import gofish.pt.entity.Review;
import gofish.pt.mapper.ReviewMapper;
import gofish.pt.security.SecurityUtils;
import gofish.pt.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final ReviewMapper reviewMapper;

    // --- CREATE ---
    @PostMapping
    public ResponseEntity<ReviewResponseDTO> createReview(@Valid @RequestBody ReviewRequestDTO request) {
        // Get authenticated user ID from JWT token
        Long authenticatedUserId = SecurityUtils.getAuthenticatedUserId();
        
        Review review = reviewService.createReview(
                authenticatedUserId,
                request.getItemId(),
                request.getRating(),
                request.getComment());

        return ResponseEntity.status(HttpStatus.CREATED).body(reviewMapper.toDTO(review));
    }

    // --- READ (single) ---
    @GetMapping("/{id}")
    public ResponseEntity<ReviewResponseDTO> getReview(@PathVariable Long id) {
        Review review = reviewService.getReview(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));
        return ResponseEntity.ok(reviewMapper.toDTO(review));
    }

    // --- READ (by item, paginated) ---
    @GetMapping("/item/{itemId}")
    public ResponseEntity<Page<ReviewResponseDTO>> getReviewsByItem(
            @PathVariable Long itemId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<Review> reviews = reviewService.getReviewsByItem(itemId, pageable);
        Page<ReviewResponseDTO> response = reviews.map(reviewMapper::toDTO);

        return ResponseEntity.ok(response);
    }

    // --- READ (by user, paginated) ---
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<ReviewResponseDTO>> getReviewsByUser(
            @PathVariable Long userId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<Review> reviews = reviewService.getReviewsByUser(userId, pageable);
        Page<ReviewResponseDTO> response = reviews.map(reviewMapper::toDTO);

        return ResponseEntity.ok(response);
    }

    // --- READ (average rating) ---
    @GetMapping("/item/{itemId}/rating")
    public ResponseEntity<Double> getAverageRating(@PathVariable Long itemId) {
        Double average = reviewService.getAverageRating(itemId);
        return ResponseEntity.ok(average != null ? average : 0.0);
    }

    // --- UPDATE ---
    @PutMapping("/{id}")
    public ResponseEntity<ReviewResponseDTO> updateReview(
            @PathVariable Long id,
            @Valid @RequestBody ReviewUpdateDTO request) {

        // Get authenticated user ID (must be the review author)
        Long authenticatedUserId = SecurityUtils.getAuthenticatedUserId();

        Review review = reviewService.updateReview(
                id,
                authenticatedUserId,
                request.getRating(),
                request.getComment());

        return ResponseEntity.ok(reviewMapper.toDTO(review));
    }

    // --- DELETE ---
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        // Get authenticated user ID (must be the review author)
        Long authenticatedUserId = SecurityUtils.getAuthenticatedUserId();

        reviewService.deleteReview(id, authenticatedUserId);
        return ResponseEntity.noContent().build();
    }
}
