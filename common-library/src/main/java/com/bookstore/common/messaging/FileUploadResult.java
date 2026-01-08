package com.bookstore.common.messaging;

import com.bookstore.common.file.FileType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private String correlationId;
    private boolean success;
    private String errorMessage;
    private FileType fileType;
    private Long entityId;
    private String sourceService;
    private String fileUrl;
    private String thumbnailUrl;
    private Long fileSize;
    private LocalDateTime uploadedAt;
}
