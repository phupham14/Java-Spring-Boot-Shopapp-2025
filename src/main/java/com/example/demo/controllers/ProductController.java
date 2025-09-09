package com.example.demo.controllers;

import com.example.demo.components.LocalizationUtils;
import com.example.demo.dtos.ProductDTO;
import com.example.demo.dtos.ProductImageDTO;
import com.example.demo.models.ProductImage;
import com.example.demo.models.Products;
import com.example.demo.responses.ProductListResponse;
import com.example.demo.responses.ProductResponse;
import com.example.demo.services.IProductService;
import com.github.javafaker.Faker;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${api.prefix}/products")
@RequiredArgsConstructor
public class ProductController {
    private static final Logger logger = Logger.getLogger(ProductController.class.getName());
    private final IProductService productService;
    private final LocalizationUtils localizationUtils;

    @PostMapping(value = "")
    public ResponseEntity<?> createProducts(
            @Valid @RequestBody ProductDTO productDTO,
            BindingResult result
    ) {
        try {
            if (result.hasErrors()) {
                List<String> errorMessages = result.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body(errorMessages);
            }

            Products newProduct = productService.createProduct(productDTO);
            return ResponseEntity.ok(newProduct);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    @PostMapping(value = "uploads/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadImages(
            @PathVariable("id") long productId,
            @RequestParam("files") List<MultipartFile> files) {
        try {
            Products existingProduct = productService.getProductById(productId);

            if (files == null || files.isEmpty()) {
                String msg = localizationUtils.getLocalizedMessage("upload.no_files");
                return ResponseEntity.badRequest().body(Map.of("message", msg));
            }

            if (files.size() > 5) {
                String msg = localizationUtils.getLocalizedMessage("upload.too_many");
                return ResponseEntity.badRequest().body(Map.of("message", msg));
            }

            List<ProductImage> productImages = new ArrayList<>();

            for (MultipartFile file : files) {
                // Kích thước
                if (file.getSize() > 10 * 1024 * 1024) {
                    String msg = localizationUtils.getLocalizedMessage("upload.too_large");
                    throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, msg);
                }

                // Định dạng
                String contentType = file.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    String msg = localizationUtils.getLocalizedMessage("upload.invalid_type");
                    throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, msg);
                }

                // Lưu file
                String filename = saveFile(file);

                // Lưu vào DB
                ProductImage productImage = productService.createProductImage(
                        existingProduct.getId(),
                        ProductImageDTO.builder().imageUrl(filename).build()
                );
                productImages.add(productImage);
            }

            return ResponseEntity.ok(productImages);

        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(Map.of("message", ex.getReason()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping(value = "/images/{imageName:.+}")
    public ResponseEntity<?> viewImage(@PathVariable("imageName") String imageName) {
        try {
            Path imagePath = Paths.get("uploads/" + imageName);
            UrlResource resource = new UrlResource(imagePath.toUri());

            if (resource.exists()) {
                String mimeType = Files.probeContentType(imagePath);
                if (mimeType == null) mimeType = "application/octet-stream";
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(mimeType))
                        .body(resource);
            } else {
                // Fallback image
                Path fallbackPath = Paths.get("uploads/Tiramisu_1426.jpeg");
                UrlResource fallbackResource = new UrlResource(fallbackPath.toUri());
                String fallbackMimeType = Files.probeContentType(fallbackPath);
                if (fallbackMimeType == null) fallbackMimeType = "image/jpeg";
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(fallbackMimeType))
                        .body(fallbackResource);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    private String saveFile(MultipartFile file) throws IOException {
        if (!isImage(file) || file.getOriginalFilename() == null) {
            throw new IOException(localizationUtils.getLocalizedMessage("upload.invalid_type"));
        }
        String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String uniqueFilename = UUID.randomUUID().toString() + "_" + fileName;
        java.nio.file.Path uploadDir = Paths.get("uploads");
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
        java.nio.file.Path destination = Paths.get(uploadDir.toString(), uniqueFilename);
        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        return uniqueFilename;
    }

    private boolean isImage(MultipartFile file ) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }

    @GetMapping("")
    public ResponseEntity<ProductListResponse> getProducts(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(value = "category_id", defaultValue = "0") Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        PageRequest pageRequest = PageRequest.of(page, size,
                //Sort.by("createdAt").descending()
                Sort.by(Sort.Direction.ASC, "id"));
        logger.info("keyword: " + keyword + ", categoryId: " + categoryId + ", page: " + page + ", size: " + size);
        Page<ProductResponse> products = productService.getAllProducts(keyword, categoryId, pageRequest);
        // Lấy tổng số trang
        int totalPages = products.getTotalPages();
        List<ProductResponse> content = products.getContent();
        return ResponseEntity.ok(ProductListResponse
                .builder()
                .products(content)
                .totalPages(totalPages)
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable("id") Long productId) {
        try {
            Products existingProduct = productService.getProductById(productId);
            return ResponseEntity.ok(ProductResponse.fromProduct(existingProduct));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.ok("Deleted Product with id:" + id + " successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/generateFakeProducts")
    public ResponseEntity<String> generateFakeProducts() {
        Faker faker = new Faker();
        for (int i = 0; i < 10000; i++) {
            String productName = faker.commerce().productName();
            if (productService.existsByName(productName)) {
                continue;
            }
            ProductDTO productDTO = ProductDTO.builder()
                    .name(productName)
                    .price((float)faker.number().numberBetween(10, 90_000_000))
                    .description(faker.lorem().sentence())
                    .thumbnail("")
                    .categoryId((long)faker.number().numberBetween(4, 8))
                    .build();

            try {
                productService.createProduct(productDTO);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        }
        return ResponseEntity.ok("Generated Fake Products");
    }

    @GetMapping("/by-ids")
    public ResponseEntity<?> getProductsByIds(@RequestParam("ids") String ids) {
        //eg: 1,3,5,7
        try {
            // Tách chuỗi ids thành một mảng các số nguyên
            List<Long> productIds = Arrays.stream(ids.split(","))
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
            List<Products> products = productService.findProductsByIds(productIds);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
