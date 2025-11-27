package gofish.pt.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ItemDTO {

    @NotBlank
    private String name;

    @NotBlank
    private String description;

    private List<String> photoUrls;

    @NotNull
    private Category category;

    @NotNull
    private Material material;

    @NotNull
    private Double price;

    @NotNull
    private Long userId;
}
