package com.bookstore.media.listener;


import com.bookstore.common.messaging.FileUploadMessage;
import com.bookstore.common.messaging.FileUploadResult;
import com.bookstore.common.messaging.RabbitMQConstants;
import com.bookstore.media.dto.UploadResult;
import com.bookstore.media.service.MediaStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class MediaUploadListener {

    private final MediaStorageService mediaStorageService;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitMQConstants.FILE_UPLOAD_QUEUE)
    public void handleUploadRequest(FileUploadMessage message) {

        try {
            UploadResult result = mediaStorageService.uploadMedia(
                    message.getFileType(),
                    message.getBase64Content(),
                    message.getOriginalFilename(),
                    message.getContentType()
            );

            FileUploadResult uploadResult = FileUploadResult.builder()
                    .correlationId(message.getCorrelationId())
                    .success(result.isSuccess())
                    .errorMessage(result.getErrorMessage())
                    .fileType(message.getFileType())
                    .entityId(message.getEntityId())
                    .sourceService(message.getSourceService())
                    .fileUrl(result.getFileUrl())
                    .thumbnailUrl(result.getThumbnailUrl())
                    .fileSize(result.getFileSize())
                    .uploadedAt(LocalDateTime.now())
                    .build();

            rabbitTemplate.convertAndSend(
                    RabbitMQConstants.FILE_EXCHANGE,
                    RabbitMQConstants.FILE_UPLOAD_RESULT_ROUTING_KEY,
                    uploadResult
            );

            if (result.isSuccess()) {
                log.info("Upload completed: {}", result.getFileUrl());
            } else {
                log.error("Upload failed: {}", result.getErrorMessage());
            }

        } catch (Exception e) {
            log.error("Error processing upload request", e);
            sendFailure(message, e.getMessage());
        }
    }

    private void sendFailure(FileUploadMessage message, String errorMessage) {
        FileUploadResult errorResult = FileUploadResult.builder()
                .correlationId(message.getCorrelationId())
                .success(false)
                .errorMessage(errorMessage)
                .fileType(message.getFileType())
                .entityId(message.getEntityId())
                .sourceService(message.getSourceService())
                .uploadedAt(LocalDateTime.now())
                .build();

        rabbitTemplate.convertAndSend(
                RabbitMQConstants.FILE_EXCHANGE,
                RabbitMQConstants.FILE_UPLOAD_RESULT_ROUTING_KEY,
                errorResult
        );
    }
}
