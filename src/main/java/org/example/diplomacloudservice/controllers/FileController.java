package org.example.diplomacloudservice.controllers;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.diplomacloudservice.exceptions.InvalidFileException;
import org.example.diplomacloudservice.utils.FileValidator;
import org.example.diplomacloudservice.dto.JsonResponse;
import org.example.diplomacloudservice.services.FileService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
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
        fileService.uploadFile(username, filename, file);
        log.info("File '{}' uploaded successfully for user '{}'", filename, username);

        return ResponseEntity.ok(new JsonResponse("File uploaded successfully", 200));
    }

    @DeleteMapping("/file")
    public ResponseEntity<JsonResponse> deleteFile(@RequestParam("filename") String filename) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.debug("Received delete request. User: {}, Filename: {}", username, filename);

        if (!fileService.fileExistsForUser(filename, username)) {
            log.warn("File '{}' does not exist for user '{}'", filename, username);
            throw new InvalidFileException("File with name '" + filename + "' does not exist for User: " + username);
        }

        fileService.deleteFile(username, filename);
        log.info("File '{}' deleted successfully for user '{}'", filename, username);

        return ResponseEntity.ok(new JsonResponse("File deleted successfully", 200));
    }
}
