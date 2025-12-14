package gofish.pt.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for deleting a review.
 * UserId is obtained from the authenticated user in SecurityContext.
 */
@Getter
@Setter
@NoArgsConstructor
public class ReviewDeleteDTO {
    // Empty body - userId comes from JWT authentication
}
