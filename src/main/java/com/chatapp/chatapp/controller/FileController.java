package com.chatapp.chatapp.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
@CrossOrigin
public class FileController {

    private static final long MAX_FILE_SIZE = 100L * 1024 * 1024;

    private final Path uploadDirectory =
            Paths.get("uploads").toAbsolutePath().normalize();

    public FileController() throws IOException {
        Files.createDirectories(uploadDirectory);
    }

    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file) {

        try {

            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("Please select a file");
            }

            if (file.getSize() > MAX_FILE_SIZE) {
                return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                        .body("File size cannot exceed 100 MB");
            }

            String originalName =
                    StringUtils.cleanPath(file.getOriginalFilename());

            if (originalName.contains("..")) {
                return ResponseEntity.badRequest()
                        .body("Invalid file name");
            }

            String extension = "";

            int dotIndex = originalName.lastIndexOf(".");

            if (dotIndex >= 0) {
                extension =
                        originalName.substring(dotIndex);
            }

            String storedName =
                    UUID.randomUUID() + extension;

            Path target =
                    uploadDirectory.resolve(storedName)
                            .normalize();

            if (!target.startsWith(uploadDirectory)) {
                return ResponseEntity.badRequest()
                        .body("Invalid file path");
            }

            Files.copy(
                    file.getInputStream(),
                    target,
                    StandardCopyOption.REPLACE_EXISTING
            );

            String fileUrl =
                    "/api/files/download/" + storedName;

            return ResponseEntity.ok(
                    new FileUploadResponse(
                            originalName,
                            storedName,
                            fileUrl,
                            file.getContentType(),
                            file.getSize()
                    )
            );

        } catch (IOException e) {

            return ResponseEntity.internalServerError()
                    .body("File upload failed: " + e.getMessage());
        }
    }

    @GetMapping("/download/{fileName}")
    public ResponseEntity<?> downloadFile(
            @PathVariable String fileName) {

        try {

            Path file =
                    uploadDirectory.resolve(fileName)
                            .normalize();

            if (!file.startsWith(uploadDirectory)) {
                return ResponseEntity.badRequest()
                        .body("Invalid file path");
            }

            if (!Files.exists(file)) {
                return ResponseEntity.notFound().build();
            }

            String contentType =
                    Files.probeContentType(file);

            if (contentType == null) {
                contentType =
                        MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            return ResponseEntity.ok()
                    .contentType(
                            MediaType.parseMediaType(contentType)
                    )
                    .body(Files.readAllBytes(file));

        } catch (IOException e) {

            return ResponseEntity.internalServerError()
                    .body("Could not download file");
        }
    }

    @DeleteMapping("/{fileName}")
    public ResponseEntity<?> deleteFile(
            @PathVariable String fileName) {

        try {

            Path file =
                    uploadDirectory.resolve(fileName)
                            .normalize();

            if (!file.startsWith(uploadDirectory)) {
                return ResponseEntity.badRequest()
                        .body("Invalid file path");
            }

            if (!Files.exists(file)) {
                return ResponseEntity.notFound().build();
            }

            Files.delete(file);

            return ResponseEntity.ok(
                    "File deleted successfully"
            );

        } catch (IOException e) {

            return ResponseEntity.internalServerError()
                    .body("Could not delete file");
        }
    }

    public record FileUploadResponse(
            String originalName,
            String storedName,
            String fileUrl,
            String contentType,
            long size
    ) {
    }
}