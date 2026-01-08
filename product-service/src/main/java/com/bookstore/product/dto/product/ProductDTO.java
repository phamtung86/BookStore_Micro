package com.bookstore.product.dto.product;

import com.bookstore.product.dto.author.AuthorDTO;
import com.bookstore.product.dto.productImage.ProductImageDTO;
import com.bookstore.product.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {

    private Long id;
    private String sku;
    private String isbn;
    private String title;
    private String slug;
    private String description;
    private String shortDescription;

    private BigDecimal originalPrice;
    private BigDecimal sellingPrice;
    private BigDecimal discountPercent;
    private BigDecimal discountAmount;
    private boolean onSale;

    private Long categoryId;
    private String categoryName;

    private Long publisherId;
    private String publisherName;

    private Long sellerId;
    private String sellerName;

    private List<AuthorDTO> authors;

    private String thumbnailUrl;
    private List<ProductImageDTO> images;

    private LocalDate publicationDate;
    private String language;
    private Integer pageCount;
    private Integer weight;
    private String dimensions;
    private String coverType;

    private Product.ProductStatus status;
    private boolean featured;
    private boolean bestseller;
    private boolean newArrival;

    private Long viewCount;
    private Long soldCount;
    private BigDecimal ratingAverage;
    private Integer ratingCount;

    private String metaTitle;
    private String metaDescription;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


}
