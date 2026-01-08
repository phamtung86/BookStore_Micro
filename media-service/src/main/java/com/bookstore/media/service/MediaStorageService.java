package com.bookstore.media.service;

import com.bookstore.common.file.FileType;
import com.bookstore.media.dto.UploadResult;
import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaStorageService {

    private final Cloudinary cloudinary;

    public UploadResult uploadMedia(FileType fileType, String base64Content,
                                    String originalFilename, String contentType) {
        try {
            byte[] fileBytes = Base64.getDecoder().decode(base64Content);
            String folder = fileType.getBucketName();
            String publicId = generatePublicId(originalFilename);
            String resourceType = determineResourceType(contentType);

            Map<String, Object> params = ObjectUtils.asMap(
                    "folder", "bookstore/" + folder,
                    "public_id", publicId,
                    "resource_type", resourceType,
                    "overwrite", true
            );

            if ("video".equals(resourceType)) {}

            Map uploadResult = cloudinary.uploader().upload(fileBytes, params);

            String secureUrl = (String) uploadResult.get("secure_url");
            String format = (String) uploadResult.get("format");
            String resultPublicId = (String) uploadResult.get("public_id");
            Long bytes = Long.valueOf(uploadResult.get("bytes").toString());

            String thumbnailUrl = null;
            if ("image".equals(resourceType)) {
                thumbnailUrl = cloudinary.url()
                        .transformation(new Transformation().width(200).height(200).crop("thumb"))
                        .generate(resultPublicId);
            } else if ("video".equals(resourceType)) {
                thumbnailUrl = cloudinary.url()
                        .resourceType("video")
                        .format("jpg")
                        .transformation(new Transformation().width(200).height(200).crop("thumb"))
                        .generate(resultPublicId);
            }

            return UploadResult.builder()
                    .success(true)
                    .fileUrl(secureUrl)
                    .thumbnailUrl(thumbnailUrl)
                    .fileSize(bytes)
                    .publicId(resultPublicId)
                    .format(format)
                    .resourceType(resourceType)
                    .build();

        } catch (IOException e) {
            log.error("Error uploading media to Cloudinary", e);
            return UploadResult.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("Unexpected error during upload", e);
            return UploadResult.builder()
                    .success(false)
                    .errorMessage("Unexpected error: " + e.getMessage())
                    .build();
        }
    }

    public void deleteMedia(String publicId, String resourceType) {
        try {
            Map params = ObjectUtils.asMap("resource_type", resourceType);
            cloudinary.uploader().destroy(publicId, params);
        } catch (Exception e) {
            log.error("Error deleting media", e);
        }
    }

    private String determineResourceType(String contentType) {
        if (contentType.startsWith("image/")) {
            return "image";
        } else if (contentType.startsWith("video/")) {
            return "video";
        } else {
            return "raw";
        }
    }

    private String generatePublicId(String originalFilename) {
        String name = "file";
        if (originalFilename != null && originalFilename.contains(".")) {
            name = originalFilename.substring(0, originalFilename.lastIndexOf("."));
        }
        name = name.replaceAll("[^a-zA-Z0-9-_]", "");
        return name + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

}
