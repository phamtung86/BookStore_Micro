package com.bookstore.product.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_products_sku", columnList = "sku"),
        @Index(name = "idx_products_isbn", columnList = "isbn"),
        @Index(name = "idx_products_slug", columnList = "slug"),
        @Index(name = "idx_products_category", columnList = "category_id"),
        @Index(name = "idx_products_status", columnList = "status"),
        @Index(name = "idx_products_price", columnList = "selling_price"),
        @Index(name = "idx_products_featured", columnList = "is_featured"),
        @Index(name = "idx_products_bestseller", columnList = "is_bestseller"),
        @Index(name = "idx_products_seller", columnList = "seller_id")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String sku;

    @Column(unique = true, length = 20)
    private String isbn;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, unique = true, length = 280)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "short_description", length = 500)
    private String shortDescription;

    // Pricing
    @Column(name = "original_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal originalPrice;

    @Column(name = "selling_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal sellingPrice;

    @Column(name = "discount_percent", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal discountPercent = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @ToString.Exclude
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publisher_id")
    @ToString.Exclude
    private Publisher publisher;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "seller_name", length = 100)
    private String sellerName;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<ProductAuthor> productAuthors = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<ProductImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    @Builder.Default
    @ToString.Exclude
    private List<ProductReview> reviews = new ArrayList<>();

    @Column(name = "publication_date")
    private LocalDate publicationDate;

    @Column(length = 50)
    @Builder.Default
    private String language = "Tiếng Việt";

    @Column(name = "page_count")
    private Integer pageCount;

    @Column
    private Integer weight;

    @Column(length = 50)
    private String dimensions;

    @Enumerated(EnumType.STRING)
    @Column(name = "cover_type")
    @Builder.Default
    private CoverType coverType = CoverType.PAPERBACK;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    // Status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ProductStatus status = ProductStatus.ACTIVE;

    @Column(name = "is_featured")
    @Builder.Default
    private Boolean isFeatured = false;

    @Column(name = "is_bestseller")
    @Builder.Default
    private Boolean isBestseller = false;

    @Column(name = "is_new_arrival")
    @Builder.Default
    private Boolean isNewArrival = false;

    @Column(name = "meta_title", length = 255)
    private String metaTitle;

    @Column(name = "meta_description", length = 500)
    private String metaDescription;

    @Column(name = "meta_keywords", length = 255)
    private String metaKeywords;

    @Column(name = "view_count")
    @Builder.Default
    private Long viewCount = 0L;

    @Column(name = "sold_count")
    @Builder.Default
    private Long soldCount = 0L;

    @Column(name = "rating_average", precision = 2, scale = 1)
    @Builder.Default
    private BigDecimal ratingAverage = BigDecimal.ZERO;

    @Column(name = "rating_count")
    @Builder.Default
    private Integer ratingCount = 0;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Soft delete fields
    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private Long deletedBy;

    // Display visibility
    @Column(name = "is_visible")
    @Builder.Default
    private Boolean isVisible = true;

    public enum ProductStatus {
        PENDING, ACTIVE, INACTIVE, OUT_OF_STOCK, DISCONTINUED, REJECTED
    }

    public enum CoverType {
        PAPERBACK, HARDCOVER, EBOOK
    }

    public void addAuthor(Author author, AuthorRole role) {
        ProductAuthor productAuthor = ProductAuthor.builder()
                .product(this)
                .author(author)
                .authorRole(role)
                .build();
        productAuthors.add(productAuthor);
    }

    public void addImage(ProductImage image) {
        images.add(image);
        image.setProduct(this);
    }

    public void incrementViewCount() {
        this.viewCount++;
    }

    public void updateRating(BigDecimal newAverage, int newCount) {
        this.ratingAverage = newAverage;
        this.ratingCount = newCount;
    }

    public BigDecimal getDiscountAmount() {
        if (discountPercent == null || discountPercent.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return originalPrice.multiply(discountPercent).divide(BigDecimal.valueOf(100));
    }

    public boolean isOnSale() {
        return sellingPrice.compareTo(originalPrice) < 0;
    }
}
