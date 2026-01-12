package com.bookstore.product.service.impl;

import com.bookstore.common.dto.response.ServiceResponse;
import com.bookstore.common.exception.BusinessException;
import com.bookstore.product.dto.author.AuthorDTO;
import com.bookstore.product.entity.Author;
import com.bookstore.product.repository.AuthorRepository;
import com.bookstore.product.service.IAuthorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthorServiceImpl implements IAuthorService {

    private final AuthorRepository authorRepository;

    @Override
    @Transactional(readOnly = true)
    public ServiceResponse getAuthorById(Long id) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Author not found: " + id));
        return ServiceResponse.RESPONSE_SUCCESS(mapToDTO(author));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Author> findById(Long id) {
        return authorRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceResponse getAuthorBySlug(String slug) {
        Author author = authorRepository.findBySlug(slug)
                .orElseThrow(() -> new BusinessException("Author not found: " + slug));
        return ServiceResponse.RESPONSE_SUCCESS(mapToDTO(author));
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceResponse getAllAuthors(Pageable pageable) {
        Page<AuthorDTO> authors = authorRepository.findAll(pageable).map(this::mapToDTO);
        return ServiceResponse.RESPONSE_SUCCESS(authors);
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceResponse searchAuthors(String name, Pageable pageable) {
        Page<AuthorDTO> authors = authorRepository.findByNameContainingIgnoreCase(name, pageable)
                .map(this::mapToDTO);
        return ServiceResponse.RESPONSE_SUCCESS(authors);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Author> findByIds(Set<Long> ids) {
        return authorRepository.findByIdIn(ids);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return authorRepository.existsByName(name);
    }

    @Override
    @Transactional
    public ServiceResponse createAuthor(AuthorDTO request) {
        if (authorRepository.existsByName(request.getName())) {
            throw new BusinessException("Author already exists: " + request.getName());
        }

        String slug = generateSlug(request.getName());
        if (authorRepository.existsBySlug(slug)) {
            slug = slug + "-" + UUID.randomUUID().toString().substring(0, 8);
        }

        Author author = Author.builder()
                .name(request.getName())
                .slug(slug)
                .build();

        Author savedAuthor = authorRepository.save(author);
        return ServiceResponse.RESPONSE_SUCCESS("Author created successfully", mapToDTO(savedAuthor));
    }

    @Override
    @Transactional
    public ServiceResponse updateAuthor(Long id, AuthorDTO request) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Author not found: " + id));

        if (!author.getName().equals(request.getName()) && authorRepository.existsByName(request.getName())) {
            throw new BusinessException("Author name already exists: " + request.getName());
        }

        author.setName(request.getName());

        Author savedAuthor = authorRepository.save(author);
        return ServiceResponse.RESPONSE_SUCCESS("Author updated successfully", mapToDTO(savedAuthor));
    }

    @Override
    @Transactional
    public ServiceResponse deleteAuthor(Long id) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Author not found: " + id));

        authorRepository.delete(author);
        return ServiceResponse.RESPONSE_SUCCESS("Author deleted successfully", null);
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

    private AuthorDTO mapToDTO(Author author) {
        return AuthorDTO.builder()
                .id(author.getId())
                .name(author.getName())
                .build();
    }
}
