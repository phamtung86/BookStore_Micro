package com.bookstore.product.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePublisherRequest {

    @NotBlank(message = "Publisher name is required")
    @Size(max = 255, message = "Publisher name must be less than 255 characters")
    private String name;

    @Size(max = 1000, message = "Description must be less than 1000 characters")
    private String description;

    private String logoUrl;

    private String website;

    private String address;

    private String email;

    private String phone;
}
