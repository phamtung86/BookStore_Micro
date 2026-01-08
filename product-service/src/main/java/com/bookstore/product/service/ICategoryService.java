package com.bookstore.product.service;

import com.bookstore.product.dto.request.CreateCategoryRequest;
import com.bookstore.product.dto.category.CategoryDTO;

import java.util.List;

public interface ICategoryService {

    CategoryDTO createCategory(CreateCategoryRequest request);

    CategoryDTO getCategoryById(Long id);

    CategoryDTO getCategoryBySlug(String slug);

    List<CategoryDTO> getAllCategories();

    List<CategoryDTO> getRootCategories();

    List<CategoryDTO> getChildCategories(Long parentId);

    CategoryDTO updateCategory(Long id, CreateCategoryRequest request);

    void deleteCategory(Long id);

    List<CategoryDTO> getCategoryTree();
}
