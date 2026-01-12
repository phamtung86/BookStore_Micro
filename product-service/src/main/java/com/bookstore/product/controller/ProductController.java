package com.bookstore.product.controller;

import com.bookstore.common.dto.response.ServiceResponse;
import com.bookstore.common.service.SharedFileService;
import com.bookstore.product.dto.product.ProductDTO;
import com.bookstore.product.dto.request.CreateProductRequest;
import com.bookstore.product.dto.request.ProductSearchCriteria;
import com.bookstore.product.service.IProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Slf4j

@Tag(name = "Product", description = "Product Management APIs")
public class ProductController {

    private final IProductService productService;
    private final SharedFileService sharedFileService;

    @PostMapping(consumes = MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Create a new product with image (Seller or Admin)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ServiceResponse> createProduct(
            @Valid @RequestPart("product") CreateProductRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestHeader("X-User-Id") Long userId) {
        ServiceResponse response = productService.createProduct(request, userId);
        ProductDTO product = (ProductDTO) response.getData();
        if (image != null && !image.isEmpty() && product != null) {
            sharedFileService.uploadFile(
                    image,
                    com.bookstore.common.file.FileType.PRODUCT_IMAGE,
                    product.getId(),
                    "product-service",
                    userId);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping(value = "/{id}", consumes = MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    @Operation(summary = "Update product (Owner or Admin)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ServiceResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestPart("product") com.bookstore.product.dto.request.UpdateProductRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(value = "X-User-Roles", required = false) String role) {

        String oldThumbnailUrl = null;
        if (image != null && !image.isEmpty()) {
            try {
                ServiceResponse currentProduct = productService.getProductById(id);
                if (currentProduct.getData() != null) {
                    ProductDTO productDTO = (ProductDTO) currentProduct.getData();
                    oldThumbnailUrl = productDTO.getThumbnailUrl();
                }
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }

        boolean isAdmin = role != null && role.contains("ADMIN");
        ServiceResponse response = productService.updateProduct(id, request, userId, isAdmin);

        if (image != null && !image.isEmpty()) {
            if (oldThumbnailUrl != null && !oldThumbnailUrl.isEmpty()) {
                sharedFileService.deleteFile(
                        oldThumbnailUrl,
                        "product-service",
                        userId);
            }

            sharedFileService.uploadFile(
                    image,
                    com.bookstore.common.file.FileType.PRODUCT_IMAGE,
                    id,
                    "product-service",
                    userId);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<ServiceResponse> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get product by slug")
    public ResponseEntity<ServiceResponse> getProductBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(productService.getProductBySlug(slug));
    }

    @GetMapping("/seller/{sellerId}")
    @Operation(summary = "Get products by seller")
    public ResponseEntity<ServiceResponse> getProductsBySeller(
            @PathVariable Long sellerId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(productService.getProductsBySeller(sellerId, pageable));
    }

    @GetMapping("/my-products")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    @Operation(summary = "Get my products (Seller or Admin)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ServiceResponse> getMyProducts(
            @RequestHeader("X-User-Id") Long userId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(productService.getMyProducts(userId, pageable));
    }

    @GetMapping("/search")
    @Operation(summary = "Advanced product search")
    public ResponseEntity<ServiceResponse> searchProducts(
            @ModelAttribute ProductSearchCriteria criteria,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(productService.searchProducts(criteria, pageable));
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get products by category")
    public ResponseEntity<ServiceResponse> getProductsByCategory(
            @PathVariable Long categoryId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(productService.getProductsByCategory(categoryId, pageable));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    @Operation(summary = "Delete product (Owner or Admin)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ServiceResponse> deleteProduct(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        boolean isAdmin = "ADMIN".equals(role);
        return ResponseEntity.ok(productService.deleteProduct(id, userId, isAdmin));
    }
}
