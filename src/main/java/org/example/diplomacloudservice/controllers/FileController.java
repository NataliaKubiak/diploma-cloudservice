package org.example.diplomacloudservice.controllers;

import lombok.AllArgsConstructor;
import org.example.diplomacloudservice.utils.FileValidator;
import org.example.diplomacloudservice.dto.JsonResponse;
import org.example.diplomacloudservice.services.FileService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
@AllArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping("/file")
    public ResponseEntity<JsonResponse> uploadFile(@RequestParam("filename") String filename,
                                                   @RequestPart("file") MultipartFile file) throws IOException {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // Валидируем: формат файла, имя, есть ли такой файл в БД у этого юзера
        FileValidator.validateFile(filename, file);
        fileService.fileExistsForUser(filename, username);

        // Если все прошло успешно, загружаем файл
        fileService.uploadFile(username, filename, file);

        return ResponseEntity.ok(new JsonResponse("File uploaded successfully", 200));
    }
}
