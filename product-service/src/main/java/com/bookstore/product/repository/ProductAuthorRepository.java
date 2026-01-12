package com.bookstore.product.repository;

import com.bookstore.product.entity.ProductAuthor;
import com.bookstore.product.entity.ProductAuthorId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductAuthorRepository extends JpaRepository<ProductAuthor, ProductAuthorId> {

    List<ProductAuthor> findByProductId(Long productId);

    List<ProductAuthor> findByAuthorId(Long authorId);

    void deleteByProductId(Long productId);
}
