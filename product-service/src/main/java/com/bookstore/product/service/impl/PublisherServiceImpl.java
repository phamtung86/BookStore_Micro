package com.bookstore.product.service.impl;

import com.bookstore.common.exception.BusinessException;
import com.bookstore.product.dto.request.CreatePublisherRequest;
import com.bookstore.product.dto.publisher.PublisherDTO;
import com.bookstore.product.entity.Publisher;
import com.bookstore.product.repository.PublisherRepository;
import com.bookstore.product.service.IPublisherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class PublisherServiceImpl implements IPublisherService {

    private final PublisherRepository publisherRepository;

    @Override
    @Transactional
    public PublisherDTO createPublisher(CreatePublisherRequest request) {
        log.info("Creating publisher: {}", request.getName());

        if (publisherRepository.existsByName(request.getName())) {
            throw new BusinessException("Publisher name already exists: " + request.getName());
        }

        String slug = generateSlug(request.getName());
        if (publisherRepository.existsBySlug(slug)) {
            slug = slug + "-" + System.currentTimeMillis();
        }

        Publisher publisher = Publisher.builder()
                .name(request.getName())
                .slug(slug)
                .description(request.getDescription())
                .logoUrl(request.getLogoUrl())
                .website(request.getWebsite())
                .address(request.getAddress())
                .build();

        Publisher saved = publisherRepository.save(publisher);
        log.info("Publisher created: {} (ID: {})", saved.getName(), saved.getId());

        return mapToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PublisherDTO getPublisherById(Long id) {
        Publisher publisher = publisherRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Publisher not found: " + id));
        return mapToDTO(publisher);
    }

    @Override
    @Transactional(readOnly = true)
    public PublisherDTO getPublisherBySlug(String slug) {
        Publisher publisher = publisherRepository.findBySlug(slug)
                .orElseThrow(() -> new BusinessException("Publisher not found: " + slug));
        return mapToDTO(publisher);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PublisherDTO> getAllPublishers(Pageable pageable) {
        return publisherRepository.findAll(pageable).map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PublisherDTO> searchPublishers(String keyword, Pageable pageable) {
        return publisherRepository.findByNameContainingIgnoreCase(keyword, pageable).map(this::mapToDTO);
    }

    @Override
    @Transactional
    public PublisherDTO updatePublisher(Long id, CreatePublisherRequest request) {
        Publisher publisher = publisherRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Publisher not found: " + id));

        if (!publisher.getName().equals(request.getName()) && publisherRepository.existsByName(request.getName())) {
            throw new BusinessException("Publisher name already exists: " + request.getName());
        }

        publisher.setName(request.getName());
        publisher.setDescription(request.getDescription());
        publisher.setLogoUrl(request.getLogoUrl());
        publisher.setWebsite(request.getWebsite());
        publisher.setAddress(request.getAddress());

        Publisher saved = publisherRepository.save(publisher);
        log.info("Publisher updated: {}", saved.getName());

        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public void deletePublisher(Long id) {
        Publisher publisher = publisherRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Publisher not found: " + id));

        publisherRepository.delete(publisher);
        log.info("Publisher deleted: {}", id);
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

    private PublisherDTO mapToDTO(Publisher publisher) {
        return PublisherDTO.builder()
                .id(publisher.getId())
                .name(publisher.getName())
                .slug(publisher.getSlug())
                .description(publisher.getDescription())
                .logoUrl(publisher.getLogoUrl())
                .website(publisher.getWebsite())
                .address(publisher.getAddress())
                .createdAt(publisher.getCreatedAt())
                .updatedAt(publisher.getUpdatedAt())
                .build();
    }
}
