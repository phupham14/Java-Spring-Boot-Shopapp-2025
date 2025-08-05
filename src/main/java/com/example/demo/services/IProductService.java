package com.example.demo.services;

import com.example.demo.dtos.ProductDTO;
import com.example.demo.dtos.ProductImageDTO;
import com.example.demo.models.ProductImage;
import com.example.demo.models.Products;
import com.example.demo.responses.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface IProductService {
    Products createProduct(ProductDTO productDTO) throws Exception;
    Products getProductById(long id) throws Exception;
    public Page<ProductResponse> getAllProducts(String keyword,
                                                Long categoryId, PageRequest pageRequest);
    Products updateProduct(long id, ProductDTO productDTO) throws Exception;
    void deleteProduct(long id);
    boolean existsByName(String name);
    ProductImage createProductImage(
            Long productId,
            ProductImageDTO productImageDTO) throws Exception;

    List<Products> findProductsByIds(List<Long> productIds);
}
