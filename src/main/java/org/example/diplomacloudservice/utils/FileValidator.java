package org.example.diplomacloudservice.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import org.example.diplomacloudservice.exceptions.InvalidFileException;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;
import java.util.regex.Pattern;

@Log4j2
@UtilityClass
public class FileValidator {

    private static final Set<String> BLOCKED_EXTENSIONS = Set.of(
            "exe", "bat", "sh", "cmd", "scr", "ps1", "jar", "msi", "vbs"
    );
    private static final Pattern FILENAME_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]+$");

    public static void validateFile(String filename, MultipartFile file) {
        log.debug("Validating file: {}", filename);
        validateFilename(filename);

        if (file.isEmpty()) {
            throw new InvalidFileException("There was no file attached");
        }

        if (isBlockedFileType(file.getOriginalFilename())) {
            log.warn("Blocked file type detected for file: {}", filename);
            throw new InvalidFileException("This file type is not allowed or file has no extension.");
        }
    }

    public static void validateFilename(String filename) {
        log.debug("Validating filename: {}", filename);
        if (filename == null || filename.isBlank()) {
            log.error("Filename is empty or blank");
            throw new InvalidFileException("Filename cannot be empty.");
        }

        if (!hasValidExtension(filename)) {
            log.error("Invalid extension format in filename: {}", filename);
            throw new InvalidFileException("Invalid extension format in filename. Only 3 or 4 characters allowed");
        }

        if (!FILENAME_PATTERN.matcher(filename).matches()) {
            log.error("Invalid characters in filename: {}", filename);
            throw new InvalidFileException("The filename contains invalid characters. Allowed characters are letters (a-z, A-Z), digits (0-9), periods (.), underscores (_), and hyphens (-). Example valid filenames: 'file_name', 'username123', 'file.name', 'a-b_c.d'");
        }

        if (isBlockedFileType(filename)) {
            log.warn("Blocked file type detected for filename: {}", filename);
            throw new InvalidFileException("This file type is not allowed or file has no extension.");
        }
        log.debug("Filename validation passed: {}", filename);
    }

    private boolean hasValidExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return false;
        }

        String extension = getFileExtension(fileName);
        return extension.matches("[a-zA-Z0-9]{3,4}"); // Проверяет 3-4 буквы/цифры
    }

    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex == -1) return "";
        return filename.substring(dotIndex + 1);
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
