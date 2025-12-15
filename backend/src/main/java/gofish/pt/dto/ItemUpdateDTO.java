package gofish.pt.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import gofish.pt.entity.Category;
import gofish.pt.entity.Material;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * DTO for partial item updates
 * All fields are optional to support partial updates
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ItemUpdateDTO {

    @Size(min = 1, max = 64, message = "Name must be between 1 and 64 characters")
    private String name;

    @Size(min = 1, max = 512, message = "Description must be between 1 and 512 characters")
    private String description;

    private List<String> photoUrls;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Category category;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Material material;

    @Positive(message = "Price must be positive")
    private Double price;

    private Boolean available;
}
