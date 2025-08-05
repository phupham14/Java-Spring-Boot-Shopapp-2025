package com.example.demo.services;

import com.example.demo.dtos.ProductDTO;
import com.example.demo.dtos.ProductImageDTO;
import com.example.demo.exceptions.DataNotFoundException;
import com.example.demo.exceptions.InvalidParamException;
import com.example.demo.models.Category;
import com.example.demo.models.ProductImage;
import com.example.demo.models.Products;
import com.example.demo.reposistories.CategoryReposistory;
import com.example.demo.reposistories.ProductImageReposistory;
import com.example.demo.reposistories.ProductReposistory;
import com.example.demo.responses.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService implements IProductService {
    private final ProductReposistory productReposistory;
    private final CategoryReposistory categoryReposistory;
    private final ProductImageReposistory productImageReposistory;

    @Override
    public Products createProduct(ProductDTO productDTO) throws DataNotFoundException {
        Category existingCategory = categoryReposistory
                .findById(productDTO.getCategoryId())
                .orElseThrow(() -> new DataNotFoundException("Category not found " + productDTO.getCategoryId()));
        Products newProduct = Products.builder()
                .name(productDTO.getName())
                .price(productDTO.getPrice())
                .thumbnail(productDTO.getThumbnail())
                .description(productDTO.getDescription())
                .category(existingCategory)
                .build();
        return productReposistory.save(newProduct);
    }

    @Override
    public Products getProductById(long productId) throws DataNotFoundException {
        Optional<Products> optionalProduct = productReposistory.getDetailProduct(productId);
        if(optionalProduct.isPresent()) {
            return optionalProduct.get();
        }
        throw new DataNotFoundException("Cannot find product with id =" + productId);
    }

    @Override
    public Page<ProductResponse> getAllProducts(String keyword, Long CategoryId, PageRequest pageRequest) {
        // Lấy ds sản phẩm theo trang (page) và limit (size)
        Page<Products> productsPage;
        productsPage = productReposistory.searchProducts(CategoryId, keyword, pageRequest);
        return productsPage.map(ProductResponse::fromProduct);
    }

    @Override
    @Transactional
    public Products updateProduct(long productId, ProductDTO productDTO) throws DataNotFoundException {
        Products productToUpdate = getProductById(productId);
        if (productToUpdate != null) {
            Category existingCategory = categoryReposistory
                    .findById(productDTO.getCategoryId())
                    .orElseThrow(() -> new DataNotFoundException("Category not found " + productDTO.getCategoryId()));
            productToUpdate.setName(productDTO.getName());
            productToUpdate.setPrice(productDTO.getPrice());
            productToUpdate.setThumbnail(productDTO.getThumbnail());
            productToUpdate.setCategory(existingCategory);
            return productReposistory.save(productToUpdate);
        }
        return null;
    }

    @Override
    public void deleteProduct(long id) {
        Optional<Products> optionalProducts = productReposistory.findById(id);
        if (optionalProducts.isPresent()) {
            productReposistory.delete(optionalProducts.get());
        }
    }

    @Override
    public boolean existsByName(String name) {
        return productReposistory.existsByName(name);
    }

    @Override
    public ProductImage createProductImage(Long productId, ProductImageDTO productImageDTO) throws Exception {
        Products existingProduct = productReposistory
                .findById(productId)
                .orElseThrow(() -> new DataNotFoundException("Product not found with id: " + productImageDTO.getProductId()));
        ProductImage productImage = ProductImage.builder()
                .product(existingProduct)
                .imageUrl(productImageDTO.getImageUrl())
                .build();
        productImageReposistory.findByProductId(productId);
        int size = productImageReposistory.findByProductId(productId).size();
        if (size > 5) {
            throw new InvalidParamException("Product can't have more than 5 images");
        }
        return productImageReposistory.save(productImage);
    }

    @Override
    public List<Products> findProductsByIds(List<Long> productIds) {
        return productReposistory.findProductsByIds(productIds);
    }
}
