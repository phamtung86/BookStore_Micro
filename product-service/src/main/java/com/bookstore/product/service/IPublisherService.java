package com.bookstore.product.service;

import com.bookstore.product.dto.request.CreatePublisherRequest;
import com.bookstore.product.dto.publisher.PublisherDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IPublisherService {

    PublisherDTO createPublisher(CreatePublisherRequest request);

    PublisherDTO getPublisherById(Long id);

    PublisherDTO getPublisherBySlug(String slug);

    Page<PublisherDTO> getAllPublishers(Pageable pageable);

    Page<PublisherDTO> searchPublishers(String keyword, Pageable pageable);

    PublisherDTO updatePublisher(Long id, CreatePublisherRequest request);

    void deletePublisher(Long id);
}
