package com.bookstore.product.service;

import com.bookstore.common.dto.response.ServiceResponse;
import com.bookstore.product.dto.request.CreateProductRequest;
import com.bookstore.product.dto.request.UpdateProductRequest;
import com.bookstore.product.dto.request.ProductSearchCriteria;
import org.springframework.data.domain.Pageable;

public interface IProductService {

    ServiceResponse createProduct(CreateProductRequest request, Long sellerId);

    ServiceResponse updateProduct(Long id, UpdateProductRequest request, Long userId, boolean isAdmin);

    ServiceResponse getProductById(Long id);

    ServiceResponse getProductBySlug(String slug);

    ServiceResponse getProductsBySeller(Long sellerId, Pageable pageable);

    ServiceResponse getMyProducts(Long sellerId, Pageable pageable);

    ServiceResponse searchProducts(ProductSearchCriteria criteria, Pageable pageable);

    ServiceResponse getProductsByCategory(Long categoryId, Pageable pageable);

    ServiceResponse deleteProduct(Long id, Long userId, boolean isAdmin);
}
