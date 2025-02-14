package org.example.diplomacloudservice.controllers;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.diplomacloudservice.dto.JsonResponse;
import org.example.diplomacloudservice.exceptions.InvalidFileException;
import org.example.diplomacloudservice.services.FileService;
import org.example.diplomacloudservice.utils.FileValidator;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Log4j2
@Controller
@AllArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping("/file")
    public ResponseEntity<JsonResponse> uploadFile(@RequestParam("filename") String filename,
                                                   @RequestPart("file") MultipartFile file) throws IOException {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.debug("Received upload request. User: {}, Filename: {}", username, filename);

        // Валидируем: формат файла, имя
        FileValidator.validateFile(filename, file);
        log.debug("File '{}' passed validation checks.", filename);

        if (fileService.fileExistsForUser(filename, username)) {
            log.warn("File '{}' already exists for user '{}'", filename, username);
            throw new InvalidFileException("File with name '" + filename + "' already exist");
        }

        // Если все прошло успешно, загружаем файл
        fileService.uploadFileForUser(username, filename, file);
        log.info("File '{}' uploaded successfully for user '{}'", filename, username);

        return ResponseEntity.ok(new JsonResponse("File uploaded successfully", 200));
    }

    @DeleteMapping("/file")
    public ResponseEntity<JsonResponse> deleteFile(@RequestParam("filename") String filename) throws IOException {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.debug("Received delete request. User: {}, Filename: {}", username, filename);

        if (!fileService.fileExistsForUser(filename, username)) {
            log.warn("File '{}' does not exist for user '{}'", filename, username);
            throw new InvalidFileException("File with name '" + filename + "' does not exist for User: " + username);
        }

        fileService.deleteFileForUser(username, filename);
        log.info("File '{}' deleted successfully for user '{}'", filename, username);

        return ResponseEntity.ok(new JsonResponse("File deleted successfully", 200));
    }

    @GetMapping("/file")
    public ResponseEntity<Resource> getFile(@RequestParam("filename") String filename) throws IOException {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Resource file = fileService.getFileForUser(username, filename);
        String contentType = fileService.getFileContentType(file);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(file);
    }
}
