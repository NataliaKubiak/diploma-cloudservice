package org.example.diplomacloudservice.controllers;

import lombok.AllArgsConstructor;
import org.example.diplomacloudservice.dto.JsonResponse;
import org.example.diplomacloudservice.services.FileService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController("/file")
@AllArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping
    public ResponseEntity<JsonResponse> uploadFile(@RequestParam("filename") String filename, // Здесь filename = "document.pdf"
                                                   @RequestPart("file") MultipartFile file) {

        // Делаем валидацию в сервисе
        fileService.validateFile(filename, file);

        // Если все прошло успешно, загружаем файл
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        fileService.uploadFile(username, filename, file);

        return ResponseEntity.ok(new JsonResponse("File uploaded successfully", 200));
    }
}
