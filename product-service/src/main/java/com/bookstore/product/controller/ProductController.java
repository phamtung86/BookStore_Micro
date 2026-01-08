package com.bookstore.product.controller;

import com.bookstore.common.dto.ApiResponse;
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
import org.springframework.data.domain.Page;
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
    public ResponseEntity<ApiResponse<ProductDTO>> createProduct(
            @Valid @RequestPart("product") CreateProductRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Name") String username) {
        ProductDTO product = productService.createProduct(request, userId, username);
        System.out.println(image);
        if (image != null && !image.isEmpty()) {
            sharedFileService.uploadFile(
                    image,
                    com.bookstore.common.file.FileType.PRODUCT_IMAGE,
                    product.getId(),
                    "product-service",
                    userId);
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product created successfully. Image processing in background.", product));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<ApiResponse<ProductDTO>> getProductById(@PathVariable Long id) {
        ProductDTO product = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get product by slug")
    public ResponseEntity<ApiResponse<ProductDTO>> getProductBySlug(@PathVariable String slug) {
        ProductDTO product = productService.getProductBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    @GetMapping("/seller/{sellerId}")
    @Operation(summary = "Get products by seller")
    public ResponseEntity<ApiResponse<Page<ProductDTO>>> getProductsBySeller(
            @PathVariable Long sellerId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ProductDTO> products = productService.getProductsBySeller(sellerId, pageable);
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/my-products")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    @Operation(summary = "Get my products (Seller or Admin)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Page<ProductDTO>>> getMyProducts(
            @RequestHeader("X-User-Id") Long userId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ProductDTO> products = productService.getMyProducts(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/search")
    @Operation(summary = "Advanced product search")
    public ResponseEntity<ApiResponse<Page<ProductDTO>>> searchProducts(
            @ModelAttribute ProductSearchCriteria criteria,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ProductDTO> products = productService.searchProducts(criteria, pageable);
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get products by category")
    public ResponseEntity<ApiResponse<Page<ProductDTO>>> getProductsByCategory(
            @PathVariable Long categoryId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ProductDTO> products = productService.getProductsByCategory(categoryId, pageable);
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    @Operation(summary = "Delete product (Owner or Admin)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        boolean isAdmin = "ADMIN".equals(role);
        productService.deleteProduct(id, userId, isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Product deleted successfully", null));
    }
}
