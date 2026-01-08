package com.bookstore.product.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductAuthorId implements Serializable {

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "author_id")
    private Long authorId;
}
