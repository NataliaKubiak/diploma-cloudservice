package org.example.diplomacloudservice.services;

import org.example.diplomacloudservice.exceptions.InvalidFileException;
import org.example.diplomacloudservice.repositories.FileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;
import java.util.regex.Pattern;

@Service
public class FileService {

    @Value("${file.storage.location:storage}")
    private String storagePath;

    private static final Set<String> BLOCKED_EXTENSIONS = Set.of(
            "exe", "bat", "sh", "cmd", "scr", "ps1", "jar", "msi", "vbs"
    );

    private static final Pattern FILENAME_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]+$");

    private final FileRepository fileRepository;

    public FileService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    public void validateFile(String filename, MultipartFile file) {
        validateFilename(filename);

        if (file.getSize() > 5 * 1024 * 1024) { // 5MB
            throw new InvalidFileException("File size exceeds the limit of 5MB");
        }

        if (isBlockedFileType(file.getOriginalFilename())) {
            throw new InvalidFileException("This file type is not allowed.");
        }
    }

    // TODO: 12/02/2025 написать метод загрузки файла
//    public String uploadFile(String username, String filename, MultipartFile file) {
//
//    }

    public void validateFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            throw new InvalidFileException("Filename cannot be empty.");
        }

        if (!FILENAME_PATTERN.matcher(filename).matches()) {
            throw new InvalidFileException("Filename contains invalid characters.");
        }

        if (isBlockedFileType(filename)) {
            throw new InvalidFileException("This file type is not allowed.");
        }
    }

    private boolean isBlockedFileType(String filename) {

        if (filename == null || !filename.contains(".")) {
            return true; // Если нет расширения, тоже блокируем
        }
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        return BLOCKED_EXTENSIONS.contains(extension);
    }
}
