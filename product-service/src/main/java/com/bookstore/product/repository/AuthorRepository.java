package com.bookstore.product.repository;

import com.bookstore.product.entity.Author;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {

    Optional<Author> findBySlug(String slug);

    boolean existsByName(String name);

    boolean existsBySlug(String slug);

    List<Author> findByIdIn(Set<Long> ids);

    Page<Author> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
