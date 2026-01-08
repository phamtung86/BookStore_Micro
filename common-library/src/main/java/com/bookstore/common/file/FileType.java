package com.bookstore.common.file;

public enum FileType {
    PRODUCT_IMAGE("product-images"),
    PRODUCT_THUMBNAIL("product-thumbnails"),
    AVATAR("avatars"),
    REVIEW_IMAGE("review-images"),
    CATEGORY_IMAGE("category-images"),
    PUBLISHER_LOGO("publisher-logos"),
    DOCUMENT("documents");

    private final String bucketName;

    FileType(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getBucketName() {
        return bucketName;
    }
}
