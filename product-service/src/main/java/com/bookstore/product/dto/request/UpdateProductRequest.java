package com.bookstore.product.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductRequest {

    @Size(max = 255, message = "Title must be less than 255 characters")
    private String title;

    @Size(max = 20, message = "ISBN must be less than 20 characters")
    private String isbn;

    private String description;

    @Size(max = 500, message = "Short description must be less than 500 characters")
    private String shortDescription;

    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal originalPrice;

    @DecimalMin(value = "0.0", message = "Discount percent must be >= 0")
    @DecimalMax(value = "100.0", message = "Discount percent must be <= 100")
    private BigDecimal discountPercent;

    private Long categoryId;

    private Long publisherId;

    private List<Long> authorIds;

    private LocalDate publicationDate;
    private String language;
    private Integer pageCount;
    private Integer weight;
    private String dimensions;
    private String coverType;

    private String metaTitle;
    private String metaDescription;
    private String metaKeywords;

    private Boolean isFeatured;
    private Boolean isBestseller;
    private Boolean isNewArrival;
    private Boolean isVisible;

    private String thumbnailUrl;
}
