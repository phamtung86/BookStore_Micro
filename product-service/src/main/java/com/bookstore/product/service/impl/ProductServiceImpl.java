package com.bookstore.product.service.impl;

import com.bookstore.common.exception.BusinessException;
import com.bookstore.product.dto.request.CreateProductRequest;
import com.bookstore.product.dto.product.ProductDTO;
import com.bookstore.product.dto.request.ProductSearchCriteria;
import com.bookstore.product.entity.Category;
import com.bookstore.product.entity.Product;
import com.bookstore.product.entity.Publisher;
import com.bookstore.product.repository.CategoryRepository;
import com.bookstore.product.repository.ProductRepository;
import com.bookstore.product.repository.PublisherRepository;
import com.bookstore.product.specification.ProductSpecification;
import com.bookstore.product.service.IProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements IProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final PublisherRepository publisherRepository;

    @Override
    @Transactional
    public ProductDTO createProduct(CreateProductRequest request, Long sellerId, String sellerName) {

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new BusinessException("Category not found: " + request.getCategoryId()));

        Publisher publisher = null;
        if (request.getPublisherId() != null) {
            publisher = publisherRepository.findById(request.getPublisherId())
                    .orElseThrow(() -> new BusinessException("Publisher not found: " + request.getPublisherId()));
        }

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
                .sellerName(sellerName)
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
        return mapToDTO(savedProduct);
    }

    @Override
    @Transactional
    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Product not found: " + id));

        productRepository.incrementViewCount(id);

        return mapToDTO(product);
    }

    @Override
    @Transactional
    public ProductDTO getProductBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new BusinessException("Product not found: " + slug));

        productRepository.incrementViewCount(product.getId());

        return mapToDTO(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> getProductsBySeller(Long sellerId, Pageable pageable) {
        return productRepository.findBySellerIdAndStatus(sellerId, Product.ProductStatus.ACTIVE, pageable)
                .map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> getMyProducts(Long sellerId, Pageable pageable) {
        return productRepository.findBySellerId(sellerId, pageable)
                .map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> searchProducts(String keyword, Pageable pageable) {
        return productRepository.searchByKeyword(keyword, Product.ProductStatus.ACTIVE, pageable)
                .map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> searchProducts(ProductSearchCriteria criteria,
                                           Pageable pageable) {
        Specification<Product> spec = ProductSpecification
                .getProducts(criteria);
        return productRepository.findAll(spec, pageable).map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> getProductsByCategory(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryIdAndStatus(categoryId, Product.ProductStatus.ACTIVE, pageable)
                .map(this::mapToDTO);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id, Long userId, boolean isAdmin) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Product not found: " + id));

        if (!product.getSellerId().equals(userId) && !isAdmin) {
            throw new BusinessException("You don't have permission to delete this product");
        }

        product.setStatus(Product.ProductStatus.DISCONTINUED);
        productRepository.save(product);

        log.info("Product deleted: {} by user ID: {}", id, userId);
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

        slug = slug.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");

        return slug;
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
