package com.bookstore.product.listener;

import com.bookstore.common.messaging.FileUploadResult;
import com.bookstore.common.messaging.RabbitMQConstants;
import com.bookstore.product.entity.Product;
import com.bookstore.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.bookstore.product.repository.ProductImageRepository; // Add import (manual handling in replacement)

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductUploadListener {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;

    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = RabbitMQConstants.FILE_UPLOAD_RESULT_QUEUE, durable = "true"), exchange = @Exchange(value = RabbitMQConstants.FILE_EXCHANGE), key = RabbitMQConstants.FILE_UPLOAD_RESULT_ROUTING_KEY))
    @Transactional
    public void handleUploadResult(FileUploadResult result) {

        if (!"product-service".equals(result.getSourceService())) {
            return;
        }

        if (result.isSuccess()) {
            try {
                Product productRef = productRepository.getReferenceById(result.getEntityId());

                com.bookstore.product.entity.ProductImage image = com.bookstore.product.entity.ProductImage.builder()
                        .product(productRef)
                        .imageUrl(result.getFileUrl())
                        .isPrimary(true)
                        .displayOrder(0)
                        .createdAt(java.time.LocalDateTime.now())
                        .build();

                productImageRepository.save(image);

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
