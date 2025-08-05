package com.example.demo.dtos;

import com.example.demo.models.Products;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ProductImageDTO {
    @JsonProperty("product_id")
    @Size(min = 1, message = "Product ID is required")
    private Long productId;

    @Size(min = 1, max = 500, message = "Image URL is required")
    @JsonProperty("image_url")
    private String imageUrl;
}
