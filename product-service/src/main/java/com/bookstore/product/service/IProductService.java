package com.bookstore.product.service;

import com.bookstore.product.dto.request.CreateProductRequest;
import com.bookstore.product.dto.product.ProductDTO;
import com.bookstore.product.dto.request.ProductSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IProductService {

    ProductDTO createProduct(CreateProductRequest request, Long sellerId, String sellerName);

    ProductDTO getProductById(Long id);

    ProductDTO getProductBySlug(String slug);

    Page<ProductDTO> getProductsBySeller(Long sellerId, Pageable pageable);

    Page<ProductDTO> getMyProducts(Long sellerId, Pageable pageable);

    Page<ProductDTO> searchProducts(String keyword, Pageable pageable);

    Page<ProductDTO> searchProducts(ProductSearchCriteria criteria,
                                    Pageable pageable);

    Page<ProductDTO> getProductsByCategory(Long categoryId, Pageable pageable);

    void deleteProduct(Long id, Long userId, boolean isAdmin);
}
