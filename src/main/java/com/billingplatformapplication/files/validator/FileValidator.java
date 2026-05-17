package com.billingplatformapplication.files.validator;



import com.billingplatformapplication.shared.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

/**
 * Validates uploaded files for security:
 * - MIME type whitelist
 * - Maximum file size
 * - Non-empty content
 */
@Slf4j
@Component
public class FileValidator {

    @Value("${files.max-size-bytes:5242880}") // 5 MB default
    private long maxSizeBytes;

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "application/pdf",
            "image/jpeg",
            "image/png",
            "image/gif",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    );

    public void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("File must not be empty");
        }
        if (file.getSize() > maxSizeBytes) {
            throw new BusinessException(
                    "File size exceeds maximum allowed: " + (maxSizeBytes / 1024 / 1024) + " MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            log.warn("Rejected file with MIME type: {}", contentType);
            throw new BusinessException(
                    "File type not allowed. Allowed types: PDF, JPEG, PNG, DOCX, XLSX");
        }
        // Basic magic-byte check for PDF
        if ("application/pdf".equals(contentType)) {
            validatePdfSignature(file);
        }
    }

    private void validatePdfSignature(MultipartFile file) {
        try {
            byte[] header = new byte[4];
            file.getInputStream().read(header);
            // PDF files start with %PDF
            if (header[0] != 0x25 || header[1] != 0x50
                    || header[2] != 0x44 || header[3] != 0x46) {
                throw new BusinessException("File content does not match declared PDF type");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error reading file header: {}", e.getMessage());
            throw new BusinessException("Cannot validate file content");
        }
    }
}

