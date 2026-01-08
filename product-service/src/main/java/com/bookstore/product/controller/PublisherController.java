package com.bookstore.product.controller;

import com.bookstore.common.dto.ApiResponse;
import com.bookstore.product.dto.request.CreatePublisherRequest;
import com.bookstore.product.dto.publisher.PublisherDTO;
import com.bookstore.product.service.IPublisherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/publishers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Publisher", description = "Publisher Management APIs")
public class PublisherController {

    private final IPublisherService publisherService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new publisher (Admin only)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<PublisherDTO>> createPublisher(
            @Valid @RequestBody CreatePublisherRequest request) {
        PublisherDTO publisher = publisherService.createPublisher(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Publisher created successfully", publisher));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get publisher by ID")
    public ResponseEntity<ApiResponse<PublisherDTO>> getPublisherById(@PathVariable Long id) {
        PublisherDTO publisher = publisherService.getPublisherById(id);
        return ResponseEntity.ok(ApiResponse.success(publisher));
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get publisher by slug")
    public ResponseEntity<ApiResponse<PublisherDTO>> getPublisherBySlug(@PathVariable String slug) {
        PublisherDTO publisher = publisherService.getPublisherBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success(publisher));
    }

    @GetMapping
    @Operation(summary = "Get all publishers")
    public ResponseEntity<ApiResponse<Page<PublisherDTO>>> getAllPublishers(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<PublisherDTO> publishers = publisherService.getAllPublishers(pageable);
        return ResponseEntity.ok(ApiResponse.success(publishers));
    }

    @GetMapping("/search")
    @Operation(summary = "Search publishers by name")
    public ResponseEntity<ApiResponse<Page<PublisherDTO>>> searchPublishers(
            @RequestParam String keyword,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<PublisherDTO> publishers = publisherService.searchPublishers(keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(publishers));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update publisher (Admin only)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<PublisherDTO>> updatePublisher(
            @PathVariable Long id,
            @Valid @RequestBody CreatePublisherRequest request) {
        PublisherDTO publisher = publisherService.updatePublisher(id, request);
        return ResponseEntity.ok(ApiResponse.success("Publisher updated successfully", publisher));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete publisher (Admin only)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> deletePublisher(@PathVariable Long id) {
        publisherService.deletePublisher(id);
        return ResponseEntity.ok(ApiResponse.success("Publisher deleted successfully", null));
    }
}
