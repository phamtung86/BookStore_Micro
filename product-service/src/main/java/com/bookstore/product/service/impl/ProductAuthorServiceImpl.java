package com.bookstore.product.service.impl;

import com.bookstore.product.dto.author.AuthorDTO;
import com.bookstore.product.entity.*;
import com.bookstore.product.repository.ProductAuthorRepository;
import com.bookstore.product.service.IAuthorService;
import com.bookstore.product.service.IProductAuthorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductAuthorServiceImpl implements IProductAuthorService {

    private final ProductAuthorRepository productAuthorRepository;
    private final IAuthorService authorService;

    @Override
    @Transactional(readOnly = true)
    public List<AuthorDTO> getAuthorsByProductId(Long productId) {
        return productAuthorRepository.findByProductId(productId)
                .stream()
                .map(pa -> AuthorDTO.builder()
                        .id(pa.getAuthor().getId())
                        .name(pa.getAuthor().getName())
                        .role(pa.getAuthorRole() != null ? pa.getAuthorRole().name() : null)
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductAuthor> findByProductId(Long productId) {
        return productAuthorRepository.findByProductId(productId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductAuthor> findByAuthorId(Long authorId) {
        return productAuthorRepository.findByAuthorId(authorId);
    }

    @Override
    @Transactional
    public ProductAuthor addAuthorToProduct(Product product, Author author, AuthorRole role) {
        ProductAuthor productAuthor = ProductAuthor.builder()
                .id(new ProductAuthorId(product.getId(), author.getId()))
                .product(product)
                .author(author)
                .authorRole(role != null ? role : AuthorRole.AUTHOR)
                .build();

        ProductAuthor saved = productAuthorRepository.save(productAuthor);
        return saved;
    }

    @Override
    @Transactional
    public void addAuthorsToProduct(Product product, List<Long> authorIds, AuthorRole defaultRole) {
        if (authorIds == null || authorIds.isEmpty()) {
            return;
        }

        Set<Long> uniqueAuthorIds = new HashSet<>(authorIds);

        List<Author> authors = authorService.findByIds(uniqueAuthorIds);

        if (authors.size() != uniqueAuthorIds.size()) {
            Set<Long> foundIds = authors.stream()
                    .map(Author::getId)
                    .collect(Collectors.toSet());

            List<Long> notFoundIds = uniqueAuthorIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .toList();

            throw new com.bookstore.common.exception.BusinessException(
                    "Authors not found: " + notFoundIds);
        }

        List<ProductAuthor> productAuthors = authors.stream()
                .map(author -> ProductAuthor.builder()
                        .id(new ProductAuthorId(product.getId(), author.getId()))
                        .product(product)
                        .author(author)
                        .authorRole(defaultRole != null ? defaultRole : AuthorRole.AUTHOR)
                        .build())
                .collect(Collectors.toList());

        productAuthorRepository.saveAll(productAuthors);
    }

    @Override
    @Transactional
    public void removeAuthorFromProduct(Long productId, Long authorId) {
        ProductAuthorId id = new ProductAuthorId(productId, authorId);
        productAuthorRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteByProductId(Long productId) {
        productAuthorRepository.deleteByProductId(productId);
    }
}
