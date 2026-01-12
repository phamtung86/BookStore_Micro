package com.bookstore.product.service.impl;

import com.bookstore.common.dto.response.ServiceResponse;
import com.bookstore.common.exception.BusinessException;
import com.bookstore.common.service.SharedFileService;
import com.bookstore.product.client.UserClient;
import com.bookstore.product.dto.author.AuthorDTO;
import com.bookstore.product.dto.product.ProductDTO;
import com.bookstore.product.dto.productImage.ProductImageDTO;
import com.bookstore.product.dto.request.CreateProductRequest;
import com.bookstore.product.dto.request.ProductSearchCriteria;
import com.bookstore.product.dto.request.UpdateProductRequest;
import com.bookstore.product.entity.AuthorRole;
import com.bookstore.product.entity.Category;
import com.bookstore.product.entity.Product;
import com.bookstore.product.entity.Publisher;
import com.bookstore.product.repository.ProductRepository;
import com.bookstore.product.service.*;
import com.bookstore.product.specification.ProductSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements IProductService {

    private final ProductRepository productRepository;
    private final ICategoryService iCategoryService;
    private final IPublisherService iPublisherService;
    private final IProductImageService productImageService;
    private final IProductAuthorService productAuthorService;
    private final UserClient userClient;
    private final SharedFileService sharedFileService;

    @Override
    @Transactional
    public ServiceResponse createProduct(CreateProductRequest request, Long sellerId) {

        Category category = null;
        if (request.getCategoryId() != null) {
            category = iCategoryService.findById(request.getCategoryId())
                    .orElseThrow(() -> new BusinessException("Category not found: " + request.getCategoryId()));
        }
        Publisher publisher = null;
        if (request.getPublisherId() != null) {
            publisher = iPublisherService.findById(request.getPublisherId())
                    .orElseThrow(() -> new BusinessException("Publisher not found: " + request.getPublisherId()));
        }

        ServiceResponse userResponse = userClient.getUserById(sellerId);
        if (userResponse == null || userResponse.getData() == null) {
            throw new BusinessException("User not found: " + sellerId);
        }
        Map<String, Object> userData = (Map<String, Object>) userResponse.getData();
        String userFullName = userData.get("fullName") != null ? userData.get("fullName").toString() : "Unknown";

        String sku = generateSku();
        String slug = generateSlug(request.getTitle());
        if (productRepository.existsBySku(sku)) {
            sku = generateSku();
        }
        if (productRepository.existsBySlug(slug)) {
            slug = slug + "-" + UUID.randomUUID().toString().substring(0, 8);
        }
        if (request.getIsbn() != null && productRepository.existsByIsbn(request.getIsbn())) {
            throw new BusinessException("ISBN already exists: " + request.getIsbn());
        }

        BigDecimal discountPercent = request.getDiscountPercent() != null ? request.getDiscountPercent()
                : BigDecimal.ZERO;
        BigDecimal sellingPrice = calculateSellingPrice(request.getOriginalPrice(), discountPercent);

        Product product = Product.builder()
                .sku(sku)
                .isbn(request.getIsbn())
                .title(request.getTitle())
                .slug(slug)
                .description(request.getDescription())
                .shortDescription(request.getShortDescription())
                .originalPrice(request.getOriginalPrice())
                .sellingPrice(sellingPrice)
                .discountPercent(discountPercent)
                .category(category)
                .publisher(publisher)
                .sellerId(sellerId)
                .sellerName(userFullName)
                .publicationDate(request.getPublicationDate())
                .language(request.getLanguage() != null ? request.getLanguage() : "Tiếng Việt")
                .pageCount(request.getPageCount())
                .weight(request.getWeight())
                .dimensions(request.getDimensions())
                .coverType(parseCoverType(request.getCoverType()))
                .metaTitle(request.getMetaTitle())
                .metaDescription(request.getMetaDescription())
                .metaKeywords(request.getMetaKeywords())
                .status(Product.ProductStatus.PENDING)
                .isFeatured(false)
                .isBestseller(false)
                .isNewArrival(true)
                .createdAt(LocalDateTime.now())
                .build();

        Product savedProduct = productRepository.save(product);

        saveProductAuthors(savedProduct, request);

        return ServiceResponse.RESPONSE_SUCCESS("Product created successfully", mapToDTO(savedProduct));
    }

    private void saveProductAuthors(Product product, CreateProductRequest request) {
        productAuthorService.addAuthorsToProduct(product, request.getAuthorIds(), AuthorRole.AUTHOR);
    }

    @Override
    @Transactional
    @Cacheable(value = "product", key = "#id")
    public ServiceResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Product not found: " + id));
        productRepository.incrementViewCount(id);
        return ServiceResponse.RESPONSE_SUCCESS(mapToDTO(product));
    }

    @Override
    @Transactional
    public ServiceResponse getProductBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new BusinessException("Product not found: " + slug));
        productRepository.incrementViewCount(product.getId());
        return ServiceResponse.RESPONSE_SUCCESS(mapToDTO(product));
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceResponse getProductsBySeller(Long sellerId, Pageable pageable) {
        Page<ProductDTO> products = productRepository
                .findBySellerIdAndStatus(sellerId, Product.ProductStatus.ACTIVE, pageable)
                .map(this::mapToDTO);
        return ServiceResponse.RESPONSE_SUCCESS(products);
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceResponse getMyProducts(Long sellerId, Pageable pageable) {
        Page<ProductDTO> products = productRepository.findBySellerId(sellerId, pageable)
                .map(this::mapToDTO);
        return ServiceResponse.RESPONSE_SUCCESS(products);
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceResponse searchProducts(ProductSearchCriteria criteria, Pageable pageable) {
        Specification<Product> spec = ProductSpecification.getProducts(criteria);
        Page<ProductDTO> products = productRepository.findAll(spec, pageable).map(this::mapToDTO);
        return ServiceResponse.RESPONSE_SUCCESS(products);
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceResponse getProductsByCategory(Long categoryId, Pageable pageable) {
        Page<ProductDTO> products = productRepository
                .findByCategoryIdAndStatus(categoryId, Product.ProductStatus.ACTIVE, pageable)
                .map(this::mapToDTO);
        return ServiceResponse.RESPONSE_SUCCESS(products);
    }

    @Override
    @Transactional
    public ServiceResponse updateProduct(Long id, UpdateProductRequest request, Long userId,
                                         boolean isAdmin) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Product not found: " + id));

        if (!product.getSellerId().equals(userId) && !isAdmin) {
            throw new BusinessException("You don't have permission to update this product");
        }

        if (request.getTitle() != null && !request.getTitle().equals(product.getTitle())) {
            product.setTitle(request.getTitle());
            String slug = generateSlug(request.getTitle());
            if (productRepository.existsBySlug(slug) && !slug.equals(product.getSlug())) {
                slug = slug + "-" + UUID.randomUUID().toString().substring(0, 8);
            }
            product.setSlug(slug);
        }

        if (request.getIsbn() != null)
            product.setIsbn(request.getIsbn());
        if (request.getDescription() != null)
            product.setDescription(request.getDescription());
        if (request.getShortDescription() != null)
            product.setShortDescription(request.getShortDescription());
        if (request.getPublicationDate() != null)
            product.setPublicationDate(request.getPublicationDate());
        if (request.getLanguage() != null)
            product.setLanguage(request.getLanguage());
        if (request.getPageCount() != null)
            product.setPageCount(request.getPageCount());
        if (request.getWeight() != null)
            product.setWeight(request.getWeight());
        if (request.getDimensions() != null)
            product.setDimensions(request.getDimensions());
        if (request.getCoverType() != null)
            product.setCoverType(parseCoverType(request.getCoverType()));
        if (request.getMetaTitle() != null)
            product.setMetaTitle(request.getMetaTitle());
        if (request.getMetaDescription() != null)
            product.setMetaDescription(request.getMetaDescription());
        if (request.getMetaKeywords() != null)
            product.setMetaKeywords(request.getMetaKeywords());
        if (request.getIsFeatured() != null)
            product.setIsFeatured(request.getIsFeatured());
        if (request.getIsBestseller() != null)
            product.setIsBestseller(request.getIsBestseller());
        if (request.getIsNewArrival() != null)
            product.setIsNewArrival(request.getIsNewArrival());
        if (request.getIsVisible() != null)
            product.setIsVisible(request.getIsVisible());
        if (request.getThumbnailUrl() != null)
            product.setThumbnailUrl(request.getThumbnailUrl());

        boolean priceChanged = false;
        if (request.getOriginalPrice() != null) {
            product.setOriginalPrice(request.getOriginalPrice());
            priceChanged = true;
        }
        if (request.getDiscountPercent() != null) {
            product.setDiscountPercent(request.getDiscountPercent());
            priceChanged = true;
        }
        if (priceChanged) {
            BigDecimal sellingPrice = calculateSellingPrice(product.getOriginalPrice(), product.getDiscountPercent());
            product.setSellingPrice(sellingPrice);
        }

        if (request.getCategoryId() != null) {
            Category category = iCategoryService.findById(request.getCategoryId())
                    .orElseThrow(() -> new BusinessException("Category not found: " + request.getCategoryId()));
            product.setCategory(category);
        }

        if (request.getPublisherId() != null) {
            Publisher publisher = iPublisherService.findById(request.getPublisherId())
                    .orElseThrow(() -> new BusinessException("Publisher not found: " + request.getPublisherId()));
            product.setPublisher(publisher);
        }

        if (request.getAuthorIds() != null) {
            productAuthorService.deleteByProductId(product.getId());
            productAuthorService.addAuthorsToProduct(product, request.getAuthorIds(), AuthorRole.AUTHOR);
        }

        Product savedProduct = productRepository.save(product);

        return ServiceResponse.RESPONSE_SUCCESS("Product updated successfully", mapToDTO(savedProduct));
    }

    @Override
    @Transactional
    @CacheEvict(value = "product", key = "#id")
    public ServiceResponse deleteProduct(Long id, Long userId, boolean isAdmin) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Product not found: " + id));

        if (!product.getSellerId().equals(userId) && !isAdmin) {
            throw new BusinessException("You don't have permission to delete this product");
        }

        if (product.getThumbnailUrl() != null && !product.getThumbnailUrl().isEmpty()) {
            sharedFileService.deleteFile(product.getThumbnailUrl(), "product-service", userId);
        }

        List<ProductImageDTO> images = productImageService.getImagesByProductId(id);
        for (ProductImageDTO img : images) {
            if (img.getImageUrl() != null && !img.getImageUrl().isEmpty()) {
                sharedFileService.deleteFile(img.getImageUrl(), "product-service", userId);
            }
        }

        productImageService.deleteByProductId(id);

        product.setIsDeleted(true);
        product.setDeletedAt(LocalDateTime.now());
        product.setDeletedBy(userId);
        product.setIsVisible(false);
        product.setStatus(Product.ProductStatus.DISCONTINUED);
        product.setThumbnailUrl(null);

        productRepository.save(product);
        return ServiceResponse.RESPONSE_SUCCESS("Product deleted successfully", null);
    }

    private String generateSku() {
        return "SKU-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String generateSlug(String title) {
        if (title == null)
            return "";
        String normalized = Normalizer.normalize(title, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String slug = pattern.matcher(normalized).replaceAll("");
        slug = slug.replace("đ", "d").replace("Đ", "D");
        return slug.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

    private BigDecimal calculateSellingPrice(BigDecimal originalPrice, BigDecimal discountPercent) {
        if (discountPercent == null || discountPercent.compareTo(BigDecimal.ZERO) == 0) {
            return originalPrice;
        }
        BigDecimal discountAmount = originalPrice.multiply(discountPercent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return originalPrice.subtract(discountAmount);
    }

    private Product.CoverType parseCoverType(String coverType) {
        if (coverType == null)
            return Product.CoverType.PAPERBACK;
        try {
            return Product.CoverType.valueOf(coverType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Product.CoverType.PAPERBACK;
        }
    }

    private ProductDTO mapToDTO(Product product) {
        List<ProductImageDTO> images = productImageService.getImagesByProductId(product.getId());

        List<AuthorDTO> authors = productAuthorService.getAuthorsByProductId(product.getId());

        return ProductDTO.builder()
                .id(product.getId())
                .sku(product.getSku())
                .isbn(product.getIsbn())
                .title(product.getTitle())
                .slug(product.getSlug())
                .description(product.getDescription())
                .shortDescription(product.getShortDescription())
                .originalPrice(product.getOriginalPrice())
                .sellingPrice(product.getSellingPrice())
                .discountPercent(product.getDiscountPercent())
                .discountAmount(product.getDiscountAmount())
                .onSale(product.isOnSale())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .publisherId(product.getPublisher() != null ? product.getPublisher().getId() : null)
                .publisherName(product.getPublisher() != null ? product.getPublisher().getName() : null)
                .sellerId(product.getSellerId())
                .sellerName(product.getSellerName())
                .thumbnailUrl(product.getThumbnailUrl())
                .images(images)
                .authors(authors)
                .publicationDate(product.getPublicationDate())
                .language(product.getLanguage())
                .pageCount(product.getPageCount())
                .weight(product.getWeight())
                .dimensions(product.getDimensions())
                .coverType(product.getCoverType() != null ? product.getCoverType().name() : null)
                .status(product.getStatus())
                .featured(Boolean.TRUE.equals(product.getIsFeatured()))
                .bestseller(Boolean.TRUE.equals(product.getIsBestseller()))
                .newArrival(Boolean.TRUE.equals(product.getIsNewArrival()))
                .viewCount(product.getViewCount())
                .soldCount(product.getSoldCount())
                .ratingAverage(product.getRatingAverage())
                .ratingCount(product.getRatingCount())
                .metaTitle(product.getMetaTitle())
                .metaDescription(product.getMetaDescription())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}