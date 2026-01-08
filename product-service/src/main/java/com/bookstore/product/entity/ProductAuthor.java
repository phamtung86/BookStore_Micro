package com.bookstore.product.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_authors")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductAuthor {

    @EmbeddedId
    private ProductAuthorId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("productId")
    @JoinColumn(name = "product_id")
    @ToString.Exclude
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("authorId")
    @JoinColumn(name = "author_id")
    @ToString.Exclude
    private Author author;

    @Enumerated(EnumType.STRING)
    @Column(name = "author_role")
    @Builder.Default
    private AuthorRole authorRole = AuthorRole.AUTHOR;

    public ProductAuthor(Product product, Author author, AuthorRole role) {
        this.product = product;
        this.author = author;
        this.authorRole = role;
        this.id = new ProductAuthorId(product.getId(), author.getId());
    }
}
