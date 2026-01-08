package com.bookstore.common.service;

import com.bookstore.common.file.FileType;
import com.bookstore.common.messaging.FileUploadMessage;
import com.bookstore.common.messaging.RabbitMQConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SharedFileService {

    private final RabbitTemplate rabbitTemplate;


    public String uploadFile(MultipartFile file, FileType fileType, Long entityId, String sourceService) {
        return uploadFile(file, fileType, entityId, sourceService, null);
    }

    public String uploadFile(MultipartFile file, FileType fileType, Long entityId,
            String sourceService, Long uploadedBy) {
        String correlationId = UUID.randomUUID().toString();

        try {
            String base64Content = Base64.getEncoder().encodeToString(file.getBytes());

            FileUploadMessage message = FileUploadMessage.builder()
                    .correlationId(correlationId)
                    .fileType(fileType)
                    .originalFilename(file.getOriginalFilename())
                    .contentType(file.getContentType())
                    .base64Content(base64Content)
                    .entityId(entityId)
                    .sourceService(sourceService)
                    .uploadedBy(uploadedBy)
                    .build();

            rabbitTemplate.convertAndSend(
                    RabbitMQConstants.FILE_EXCHANGE,
                    RabbitMQConstants.FILE_UPLOAD_ROUTING_KEY,
                    message);

            return correlationId;
        } catch (IOException e) {
            log.error("Failed to read file content", e);
            throw new RuntimeException("Failed to prepare file for upload", e);
        }
    }
}
