package com.bookstore.product.dto.category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {

    private Long id;
    private String name;
    private String slug;
    private String description;
    private Long parentId;
    private String parentName;
    private String imageUrl;
    private Integer displayOrder;
    private Boolean isActive;
    private Long productCount;
    private List<CategoryDTO> children;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
