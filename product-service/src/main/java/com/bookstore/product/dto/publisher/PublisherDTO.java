package com.bookstore.product.dto.publisher;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublisherDTO {

    private Long id;
    private String name;
    private String slug;
    private String description;
    private String logoUrl;
    private String website;
    private String address;
    private String email;
    private String phone;
    private Long bookCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
