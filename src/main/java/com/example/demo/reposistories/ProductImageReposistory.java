package com.example.demo.reposistories;

import com.example.demo.models.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductImageReposistory extends JpaRepository<ProductImage, Long> {
    List<ProductImage> findByProductId(Long productId);
}
