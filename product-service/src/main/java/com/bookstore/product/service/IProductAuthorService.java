package com.bookstore.product.service;

import com.bookstore.product.dto.author.AuthorDTO;
import com.bookstore.product.entity.Author;
import com.bookstore.product.entity.AuthorRole;
import com.bookstore.product.entity.Product;
import com.bookstore.product.entity.ProductAuthor;

import java.util.List;

public interface IProductAuthorService {

    List<AuthorDTO> getAuthorsByProductId(Long productId);

    List<ProductAuthor> findByProductId(Long productId);

    List<ProductAuthor> findByAuthorId(Long authorId);

    ProductAuthor addAuthorToProduct(Product product, Author author, AuthorRole role);

    void addAuthorsToProduct(Product product, List<Long> authorIds, AuthorRole defaultRole);

    void removeAuthorFromProduct(Long productId, Long authorId);

    void deleteByProductId(Long productId);
}
