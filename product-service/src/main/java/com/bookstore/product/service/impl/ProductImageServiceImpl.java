package com.bookstore.product.service.impl;

import com.bookstore.common.dto.response.ServiceResponse;
import com.bookstore.common.exception.BusinessException;
import com.bookstore.product.dto.productImage.ProductImageDTO;
import com.bookstore.product.entity.Product;
import com.bookstore.product.entity.ProductImage;
import com.bookstore.product.repository.ProductImageRepository;
import com.bookstore.product.repository.ProductRepository;
import com.bookstore.product.service.IProductImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductImageServiceImpl implements IProductImageService {

    private final ProductImageRepository productImageRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ProductImageDTO> getImagesByProductId(Long productId) {
        return productImageRepository.findByProductId(productId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductImage> findByProductId(Long productId) {
        return productImageRepository.findByProductId(productId);
    }

    @Override
    @Transactional
    public ServiceResponse addProductImage(Long productId, ProductImageDTO request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("Product not found: " + productId));

        ProductImage image = ProductImage.builder()
                .product(product)
                .imageUrl(request.getImageUrl())
                .altText(request.getAltText())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .createdAt(LocalDateTime.now())
                .build();

        ProductImage savedImage = productImageRepository.save(image);
        return ServiceResponse.RESPONSE_SUCCESS("Image added successfully", mapToDTO(savedImage));
    }

    @Override
    @Transactional
    public ServiceResponse updateProductImage(Long imageId, ProductImageDTO request) {
        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new BusinessException("Image not found: " + imageId));

        if (request.getImageUrl() != null) {
            image.setImageUrl(request.getImageUrl());
        }
        if (request.getAltText() != null) {
            image.setAltText(request.getAltText());
        }
        if (request.getDisplayOrder() != null) {
            image.setDisplayOrder(request.getDisplayOrder());
        }

        ProductImage savedImage = productImageRepository.save(image);
        return ServiceResponse.RESPONSE_SUCCESS("Image updated successfully", mapToDTO(savedImage));
    }

    @Override
    @Transactional
    public ServiceResponse deleteProductImage(Long imageId) {
        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new BusinessException("Image not found: " + imageId));

        productImageRepository.delete(image);
        return ServiceResponse.RESPONSE_SUCCESS("Image deleted successfully", null);
    }

    @Override
    @Transactional
    public ServiceResponse deleteByProductId(Long productId) {
        productImageRepository.deleteByProductId(productId);
        return ServiceResponse.RESPONSE_SUCCESS("All images deleted successfully", null);
    }

    private ProductImageDTO mapToDTO(ProductImage image) {
        return ProductImageDTO.builder()
                .id(image.getId())
                .imageUrl(image.getImageUrl())
                .altText(image.getAltText())
                .displayOrder(image.getDisplayOrder())
                .build();
    }
}
