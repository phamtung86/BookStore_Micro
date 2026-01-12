package com.bookstore.common.messaging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileDeleteMessage implements Serializable {
    private String fileUrl;
    private String publicId; 
    private String sourceService;
    private Long deletedBy;
}
