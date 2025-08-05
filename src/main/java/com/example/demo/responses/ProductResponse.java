package com.example.demo.responses;

import com.example.demo.models.ProductImage;
import com.example.demo.models.Products;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductResponse extends BaseResponse {
    private String name;
    private Float price;
    private String thumbnail;
    private String description;
    @JsonProperty("category_id")
    private Long categoryId;
    private List<ProductImage> productImages = new ArrayList<>();

    public static ProductResponse fromProduct(Products products) {
        ProductResponse productResponse = ProductResponse.builder()
                .name(products.getName())
                .price(products.getPrice())
                .thumbnail(products.getThumbnail())
                .description(products.getDescription())
                .categoryId(products.getCategory().getId())
                .build();
        productResponse.setCreatedAt(products.getCreatedAt());
        productResponse.setUpdatedAt(products.getUpdatedAt());
        return productResponse;
    }
}
