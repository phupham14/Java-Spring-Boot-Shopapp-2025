package com.example.demo.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetailDTO {
    @JsonProperty("order_id")
    @Min(value = 1, message = "Order ID must be greater than 0")
    private Long orderId;

    @JsonProperty("product_id")
    @Min(value = 1, message = "product ID must be greater than 0")
    private Long productId;

    @JsonProperty("number_of_products")
    @Min(value = 1, message = "Number of products must be greater than 0")
    private int numberOfProducts;

    private String color;
}
