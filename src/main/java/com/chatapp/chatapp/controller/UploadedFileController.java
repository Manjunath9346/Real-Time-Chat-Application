package com.chatapp.chatapp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/uploaded-files")
@CrossOrigin
public class UploadedFileController {

    private final Path uploadDirectory =
            Paths.get("uploads").toAbsolutePath().normalize();

    public UploadedFileController() throws IOException {
        Files.createDirectories(uploadDirectory);
    }

    @GetMapping
    public ResponseEntity<?> getUploadedFiles() {

        try {

            List<FileInfo> files = new ArrayList<>();

            try (DirectoryStream<Path> stream =
                         Files.newDirectoryStream(uploadDirectory)) {

                for (Path path : stream) {

                    if (Files.isRegularFile(path)) {

                        files.add(
                                new FileInfo(
                                        path.getFileName().toString(),
                                        Files.size(path),
                                        "/api/files/download/" +
                                                path.getFileName()
                                                        .toString()
                                )
                        );
                    }
                }
            }

            return ResponseEntity.ok(files);

        } catch (IOException e) {

            return ResponseEntity.internalServerError()
                    .body("Could not read uploaded files");
        }
    }

    @GetMapping("/{fileName}")
    public ResponseEntity<?> getFileInfo(
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

            return ResponseEntity.ok(
                    new FileInfo(
                            file.getFileName().toString(),
                            Files.size(file),
                            "/api/files/download/" + fileName
                    )
            );

        } catch (IOException e) {

            return ResponseEntity.internalServerError()
                    .body("Could not read file information");
        }
    }

    @DeleteMapping("/{fileName}")
    public ResponseEntity<?> deleteUploadedFile(
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
                    "Uploaded file deleted successfully"
            );

        } catch (IOException e) {

            return ResponseEntity.internalServerError()
                    .body("Could not delete file");
        }
    }

    public record FileInfo(
            String fileName,
            long size,
            String downloadUrl
    ) {
    }
}