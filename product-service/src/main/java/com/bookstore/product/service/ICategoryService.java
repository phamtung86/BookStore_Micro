package com.bookstore.product.service;

import com.bookstore.common.dto.response.ServiceResponse;
import com.bookstore.product.dto.request.CreateCategoryRequest;
import com.bookstore.product.entity.Category;

import java.util.Optional;

public interface ICategoryService {

    ServiceResponse createCategory(CreateCategoryRequest request);

    ServiceResponse getCategoryById(Long id);

    /**
     * Get category entity by ID (for internal service use)
     */
    Optional<Category> findById(Long id);

    ServiceResponse getCategoryBySlug(String slug);

    ServiceResponse getAllCategories();

    ServiceResponse getRootCategories();

    ServiceResponse getChildCategories(Long parentId);

    ServiceResponse updateCategory(Long id, CreateCategoryRequest request);

    ServiceResponse deleteCategory(Long id);

    ServiceResponse getCategoryTree();
}
