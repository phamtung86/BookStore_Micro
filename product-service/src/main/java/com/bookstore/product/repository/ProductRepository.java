package com.bookstore.product.repository;

import com.bookstore.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    Optional<Product> findBySku(String sku);

    Optional<Product> findBySlug(String slug);

    Optional<Product> findByIsbn(String isbn);

    boolean existsBySku(String sku);

    boolean existsBySlug(String slug);

    boolean existsByIsbn(String isbn);

    @Modifying
    @Query("UPDATE Product p SET p.thumbnailUrl = :thumbnailUrl, p.status = :status WHERE p.id = :id")
    void updateProductImageAndStatus(Long id, String thumbnailUrl,
            Product.ProductStatus status);

    Page<Product> findBySellerId(Long sellerId, Pageable pageable);

    Page<Product> findBySellerIdAndStatus(Long sellerId, Product.ProductStatus status, Pageable pageable);

    Page<Product> findByCategoryIdAndStatus(Long categoryId, Product.ProductStatus status, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE " +
            "(LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND p.status = :status")
    Page<Product> searchByKeyword(@Param("keyword") String keyword,
            @Param("status") Product.ProductStatus status,
            Pageable pageable);

    @Modifying
    @Query("UPDATE Product p SET p.viewCount = p.viewCount + 1 WHERE p.id = :id")
    void incrementViewCount(@Param("id") Long id);

    long countBySellerId(Long sellerId);
}
