package com.example.demo.reposistories;

import com.example.demo.models.Products;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductReposistory extends JpaRepository<Products, Long> {
    boolean existsByName(String name);
    Page<Products> findAll(Pageable pageable); //Phân trang

    @Query("SELECT p FROM Products p WHERE " +
            "(:categoryId IS NULL OR :categoryId = 0 OR p.category.id = :categoryId) " +
            "AND (" +
            ":keyword IS NULL OR :keyword = '' OR " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))" +
            ")")
    Page<Products> searchProducts
            (@Param("categoryId") Long categoryId,
             @Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT p FROM Products p LEFT JOIN FETCH p.productImages WHERE p.id = :productId")
    Optional<Products> getDetailProduct(@Param("productId") Long productId);

    @Query("SELECT p FROM Products p WHERE p.id IN :productIds")
    List<Products> findProductsByIds(@Param("productIds") List<Long> productIds);
}
