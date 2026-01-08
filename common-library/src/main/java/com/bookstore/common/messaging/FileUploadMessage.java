package com.bookstore.common.messaging;

import com.bookstore.common.file.FileType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private String correlationId;
    private FileType fileType;
    private String originalFilename;
    private String contentType;
    private String base64Content;
    private Long entityId;
    private String sourceService;
    private Integer resizeWidth;
    private Integer resizeHeight;
    private Long uploadedBy;
}
