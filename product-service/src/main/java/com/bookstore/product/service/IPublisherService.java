package com.bookstore.product.service;

import com.bookstore.common.dto.response.ServiceResponse;
import com.bookstore.product.dto.request.CreatePublisherRequest;
import com.bookstore.product.entity.Publisher;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface IPublisherService {

    ServiceResponse createPublisher(CreatePublisherRequest request);

    ServiceResponse getPublisherById(Long id);

    /**
     * Get publisher entity by ID (for internal service use)
     */
    Optional<Publisher> findById(Long id);

    ServiceResponse getPublisherBySlug(String slug);

    ServiceResponse getAllPublishers(Pageable pageable);

    ServiceResponse searchPublishers(String keyword, Pageable pageable);

    ServiceResponse updatePublisher(Long id, CreatePublisherRequest request);

    ServiceResponse deletePublisher(Long id);
}
