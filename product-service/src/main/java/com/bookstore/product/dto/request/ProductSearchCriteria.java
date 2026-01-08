package com.bookstore.product.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductSearchCriteria {
    private String keyword;
    private Long categoryId;
    private Long publisherId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String sort;
}
