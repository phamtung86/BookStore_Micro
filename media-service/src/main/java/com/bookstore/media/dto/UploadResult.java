package com.bookstore.media.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadResult {
    private boolean success;
    private String errorMessage;
    private String fileUrl;
    private String thumbnailUrl;
    private Long fileSize;
    private String publicId;
    private String format;
    private String resourceType;
}