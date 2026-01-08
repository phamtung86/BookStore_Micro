package com.bookstore.product.repository;

import com.bookstore.product.entity.Publisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PublisherRepository extends JpaRepository<Publisher, Long> {

    Optional<Publisher> findBySlug(String slug);

    boolean existsByName(String name);

    boolean existsBySlug(String slug);

    Page<Publisher> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
