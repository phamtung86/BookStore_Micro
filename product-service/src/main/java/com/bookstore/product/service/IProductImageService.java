package com.bookstore.product.service;

import com.bookstore.common.dto.response.ServiceResponse;
import com.bookstore.product.dto.productImage.ProductImageDTO;
import com.bookstore.product.entity.ProductImage;

import java.util.List;

public interface IProductImageService {

    List<ProductImageDTO> getImagesByProductId(Long productId);

    List<ProductImage> findByProductId(Long productId);

    ServiceResponse addProductImage(Long productId, ProductImageDTO request);

    ServiceResponse updateProductImage(Long imageId, ProductImageDTO request);

    ServiceResponse deleteProductImage(Long imageId);

    ServiceResponse deleteByProductId(Long productId);
}
