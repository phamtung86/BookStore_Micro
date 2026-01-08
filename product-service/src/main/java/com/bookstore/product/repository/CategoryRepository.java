package com.bookstore.product.repository;

import com.bookstore.product.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findBySlug(String slug);

    List<Category> findByParentIsNullOrderByDisplayOrderAsc();

    List<Category> findByParentIdOrderByDisplayOrderAsc(Long parentId);

    List<Category> findByIsActiveTrueOrderByDisplayOrderAsc();

    boolean existsBySlug(String slug);

    boolean existsByName(String name);

    @Query("SELECT c FROM Category c WHERE c.isActive = true AND c.parent IS NULL ORDER BY c.displayOrder")
    List<Category> findRootCategories();

    long countByParentId(Long parentId);
}
