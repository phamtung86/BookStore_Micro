package com.bookstore.product.listener;

import com.bookstore.common.messaging.FileUploadResult;
import com.bookstore.common.messaging.RabbitMQConstants;
import com.bookstore.product.dto.productImage.ProductImageDTO;
import com.bookstore.product.entity.Product;
import com.bookstore.product.entity.ProductImage;
import com.bookstore.product.repository.ProductRepository;
import com.bookstore.product.service.IProductImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.bookstore.product.repository.ProductImageRepository; // Add import (manual handling in replacement)

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductUploadListener {

    private final ProductRepository productRepository;
    private final IProductImageService iProductImageService;

    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = RabbitMQConstants.FILE_UPLOAD_RESULT_QUEUE, durable = "true"), exchange = @Exchange(value = RabbitMQConstants.FILE_EXCHANGE), key = RabbitMQConstants.FILE_UPLOAD_RESULT_ROUTING_KEY))
    @Transactional
    public void handleUploadResult(FileUploadResult result) {

        if (!"product-service".equals(result.getSourceService())) {
            return;
        }

        if (result.isSuccess()) {
            try {
                Product productRef = productRepository.getReferenceById(result.getEntityId());

                ProductImageDTO productImageDTO = new ProductImageDTO(null, result.getFileUrl(), "", 0);

                iProductImageService.addProductImage(productRef.getId(),productImageDTO);

                productRepository.updateProductImageAndStatus(
                        result.getEntityId(),
                        result.getFileUrl(),
                        Product.ProductStatus.ACTIVE);

            } catch (Exception e) {
                log.error("Error updating product image: {}", e.getMessage());
            }
        } else {
            log.error("Upload failed for product {}: {}", result.getEntityId(), result.getErrorMessage());
        }
    }
}
