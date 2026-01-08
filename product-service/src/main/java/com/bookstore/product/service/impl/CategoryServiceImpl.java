package com.bookstore.product.service.impl;

import com.bookstore.common.exception.BusinessException;
import com.bookstore.product.dto.request.CreateCategoryRequest;
import com.bookstore.product.dto.category.CategoryDTO;
import com.bookstore.product.entity.Category;
import com.bookstore.product.repository.CategoryRepository;
import com.bookstore.product.repository.ProductRepository;
import com.bookstore.product.service.ICategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements ICategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public CategoryDTO createCategory(CreateCategoryRequest request) {
        log.info("Creating category: {}", request.getName());

        if (categoryRepository.existsByName(request.getName())) {
            throw new BusinessException("Category name already exists: " + request.getName());
        }

        String slug = generateSlug(request.getName());
        if (categoryRepository.existsBySlug(slug)) {
            slug = slug + "-" + System.currentTimeMillis();
        }

        Category parent = null;
        if (request.getParentId() != null) {
            parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new BusinessException("Parent category not found: " + request.getParentId()));
        }

        Category category = Category.builder()
                .name(request.getName())
                .slug(slug)
                .description(request.getDescription())
                .parent(parent)
                .imageUrl(request.getImageUrl())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .isActive(true)
                .build();

        Category saved = categoryRepository.save(category);
        return mapToDTO(saved, parent);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Category not found: " + id));
        return mapToDTO(category, getParent(category));
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDTO getCategoryBySlug(String slug) {
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new BusinessException("Category not found: " + slug));
        return mapToDTO(category, getParent(category));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findByIsActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(c -> mapToDTO(c, getParent(c)))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> getRootCategories() {
        return categoryRepository.findByParentIsNullOrderByDisplayOrderAsc()
                .stream()
                .map(c -> mapToDTO(c, null))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> getChildCategories(Long parentId) {
        return categoryRepository.findByParentIdOrderByDisplayOrderAsc(parentId)
                .stream()
                .map(c -> mapToDTO(c, getParent(c)))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CategoryDTO updateCategory(Long id, CreateCategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Category not found: " + id));

        if (!category.getName().equals(request.getName()) && categoryRepository.existsByName(request.getName())) {
            throw new BusinessException("Category name already exists: " + request.getName());
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setImageUrl(request.getImageUrl());
        if (request.getParentId() != null) {
            Category newParent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new BusinessException("Parent category not found: " + request.getParentId()));
            category.setParent(newParent);
        } else {
            category.setParent(null);
        }
        if (request.getDisplayOrder() != null) {
            category.setDisplayOrder(request.getDisplayOrder());
        }

        Category saved = categoryRepository.save(category);
        return mapToDTO(saved, getParent(saved));
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Category not found: " + id));

        long childCount = categoryRepository.countByParentId(id);
        if (childCount > 0) {
            throw new BusinessException("Cannot delete category with child categories");
        }

        category.setIsActive(false);
        categoryRepository.save(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> getCategoryTree() {
        List<Category> rootCategories = categoryRepository.findByParentIsNullOrderByDisplayOrderAsc();
        return rootCategories.stream()
                .map(this::mapToDTOWithChildren)
                .collect(Collectors.toList());
    }

    private CategoryDTO mapToDTOWithChildren(Category category) {
        List<Category> children = categoryRepository.findByParentIdOrderByDisplayOrderAsc(category.getId());
        List<CategoryDTO> childDTOs = children.stream()
                .map(this::mapToDTOWithChildren)
                .collect(Collectors.toList());

        CategoryDTO dto = mapToDTO(category, null);
        dto.setChildren(childDTOs);
        return dto;
    }

    private Category getParent(Category category) {
        return category.getParent();
    }

    private String generateSlug(String name) {
        if (name == null)
            return "";
        String normalized = Normalizer.normalize(name, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String slug = pattern.matcher(normalized).replaceAll("");
        slug = slug.replace("đ", "d").replace("Đ", "D");
        return slug.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

    private CategoryDTO mapToDTO(Category category, Category parent) {
        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .parentId(parent != null ? parent.getId() : null)
                .parentName(parent != null ? parent.getName() : null)
                .imageUrl(category.getImageUrl())
                .displayOrder(category.getDisplayOrder())
                .isActive(category.getIsActive())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}
