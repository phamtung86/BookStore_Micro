package com.bookstore.product.service;

import com.bookstore.common.dto.response.ServiceResponse;
import com.bookstore.product.dto.author.AuthorDTO;
import com.bookstore.product.entity.Author;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface IAuthorService {

    ServiceResponse getAuthorById(Long id);

    Optional<Author> findById(Long id);

    ServiceResponse getAuthorBySlug(String slug);

    ServiceResponse getAllAuthors(Pageable pageable);

    ServiceResponse searchAuthors(String name, Pageable pageable);

    List<Author> findByIds(Set<Long> ids);

    boolean existsByName(String name);

    ServiceResponse createAuthor(AuthorDTO request);

    ServiceResponse updateAuthor(Long id, AuthorDTO request);

    ServiceResponse deleteAuthor(Long id);
}
