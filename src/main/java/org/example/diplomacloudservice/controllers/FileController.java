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
import java.util.Map;

@Log4j2
@Controller
@AllArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping("/file")
    public ResponseEntity<JsonResponse> uploadFile(@RequestParam("filename") String filename,
                                                   @RequestPart("file") MultipartFile file) throws IOException {
        String username = getUsername();
        log.debug("Received upload request. User: {}, Filename: {}", username, filename);

        FileValidator.validateFile(filename, file);
        log.debug("File '{}' passed validation checks.", filename);

        if (fileService.fileExistsForUser(filename, username)) {
            log.warn("File '{}' already exists for user '{}'", filename, username);
            throw new InvalidFileException("File with name '" + filename + "' already exist");
        }

        fileService.uploadFileForUser(username, filename, file);
        log.info("File '{}' uploaded successfully for user '{}'", filename, username);

        return ResponseEntity.ok(new JsonResponse("File uploaded successfully", 200));
    }

    @DeleteMapping("/file")
    public ResponseEntity<JsonResponse> deleteFile(@RequestParam("filename") String filename) throws IOException {
        String username = getUsername();
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
    public ResponseEntity<Resource> downloadFile(@RequestParam("filename") String filename) throws IOException {
        String username = getUsername();

        if (!fileService.fileExistsForUser(filename, username)) {
            log.warn("File '{}' does not exist for user '{}'", filename, username);
            throw new InvalidFileException("File with name '" + filename + "' does not exist for User: " + username);
        }

        Resource file = fileService.getFileForUser(username, filename);
        String contentType = fileService.getFileContentType(file);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(file);
    }

    @PutMapping("/file")
    public ResponseEntity<JsonResponse> renameFile(@RequestParam("filename") String oldFilename,
                                                   @RequestBody Map<String, String> newFilenameRequest) throws IOException {
        String username = getUsername();
        String newFilename = newFilenameRequest.get("name");

        FileValidator.validateFilename(newFilename);

        if (!fileService.fileExistsForUser(oldFilename, username)) {
            log.warn("File '{}' does not exist for user '{}'", oldFilename, username);
            throw new InvalidFileException("File with name '" + oldFilename + "' does not exist for User: " + username);
        }

        fileService.renameFileForUser(username, oldFilename, newFilename);
        log.info("File renamed successfully from '{}' to '{}'", oldFilename, newFilename);

        return ResponseEntity.ok(new JsonResponse("File renamed successfully", 200));
    }

    private String getUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
