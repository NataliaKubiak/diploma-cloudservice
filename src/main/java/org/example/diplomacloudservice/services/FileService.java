package org.example.diplomacloudservice.services;

import lombok.extern.log4j.Log4j2;
import org.example.diplomacloudservice.entities.File;
import org.example.diplomacloudservice.entities.User;
import org.example.diplomacloudservice.exceptions.InvalidFileException;
import org.example.diplomacloudservice.repositories.FileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.regex.Pattern;

@Log4j2
@Service
@Transactional(readOnly = true)
public class FileService {

    @Value("${file.storage.location:storage}")
    private String storagePath;

    private static final Set<String> BLOCKED_EXTENSIONS = Set.of(
            "exe", "bat", "sh", "cmd", "scr", "ps1", "jar", "msi", "vbs"
    );
    private static final Pattern FILENAME_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]+$");

    private final FileRepository fileRepository;
    private final UserService userService;

    public FileService(FileRepository fileRepository, UserService userService) {
        this.fileRepository = fileRepository;
        this.userService = userService;
    }

    public void validateFile(String filename, MultipartFile file) {
        log.debug("Validating file: {}", filename);
        validateFilename(filename);

        if (file.getSize() > 5 * 1024 * 1024) { // 5MB
            log.warn("File size exceeds the limit: {} MB", file.getSize() / 1024 / 1024);
            throw new InvalidFileException("File size exceeds the limit of 5MB");
        }

        if (isBlockedFileType(file.getOriginalFilename())) {
            log.warn("Blocked file type detected for file: {}", filename);
            throw new InvalidFileException("This file type is not allowed.");
        }
    }

    @Transactional
    public void uploadFile(String username, String filename, MultipartFile multipartFile) throws IOException {
        log.debug("Starting file upload for user: {}, filename: {}", username, filename);
        User fileOwner = userService.getUserByUsername(username);

        //создаем папку пользователя
        Path userDir = Paths.get(storagePath, "user_" + fileOwner.getId());
        log.debug("User directory: {}", userDir.toString());

        Files.createDirectories(userDir);
        log.debug("User Directory created");

        //сохраняем файл на диск
        Path filePath = userDir.resolve(filename);
        log.debug("File path: {}", filePath.toString());

        multipartFile.transferTo(filePath.toFile());
        log.debug("File created.");

        //сохраняем инфу в БД
        File fileEntity = File.builder()
                .fileName(filename)
                .user(fileOwner)
                .fileLocation(userDir.toString())
                .size(multipartFile.getSize())
                .createdAt(LocalDateTime.now())
                .build();

        fileRepository.save(fileEntity);
        log.debug("File Entity save in DB: {}", fileEntity.toString());
    }

    public void validateFilename(String filename) {
        log.debug("Validating filename: {}", filename);
        if (filename == null || filename.isBlank()) {
            log.error("Filename is empty or blank");
            throw new InvalidFileException("Filename cannot be empty.");
        }

        if (!FILENAME_PATTERN.matcher(filename).matches()) {
            log.error("Invalid characters in filename: {}", filename);
            throw new InvalidFileException("Filename contains invalid characters.");
        }

        if (isBlockedFileType(filename)) {
            log.warn("Blocked file type detected for filename: {}", filename);
            throw new InvalidFileException("This file type is not allowed.");
        }
        log.debug("Filename validation passed: {}", filename);
    }

    private boolean isBlockedFileType(String filename) {
        if (filename == null || !filename.contains(".")) {
            log.warn("File has no extension, blocking upload: {}", filename);
            return true; // Если нет расширения, тоже блокируем
        }

        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        boolean isBlocked = BLOCKED_EXTENSIONS.contains(extension);

        if (isBlocked) {
            log.warn("Blocked file extension: {}", extension);
        }
        return isBlocked;
    }
}
