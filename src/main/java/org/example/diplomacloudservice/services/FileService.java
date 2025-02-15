package org.example.diplomacloudservice.services;

import lombok.extern.log4j.Log4j2;
import org.example.diplomacloudservice.dto.FileInfoDto;
import org.example.diplomacloudservice.entities.File;
import org.example.diplomacloudservice.entities.User;
import org.example.diplomacloudservice.exceptions.FileStorageException;
import org.example.diplomacloudservice.repositories.FileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Log4j2
@Service
@Transactional(readOnly = true)
public class FileService {

    @Value("${file.storage.location:storage}")
    private String storagePath;

    private final FileRepository fileRepository;
    private final UserService userService;

    public FileService(FileRepository fileRepository, UserService userService) {
        this.fileRepository = fileRepository;
        this.userService = userService;
    }

    public boolean fileExistsForUser(String filename, String username) {
        User owner = userService.getUserByUsername(username);
        Optional<File> maybeFile = fileRepository.findByFileNameAndUserId(filename, owner.getId());

        return maybeFile.isPresent();
    }

    @Transactional
    public void uploadFileForUser(String username, String filename, MultipartFile multipartFile) throws IOException {
        log.debug("Starting file upload for user: {}, filename: {}", username, filename);
        User fileOwner = userService.getUserByUsername(username);

        Path userDir = Paths.get(storagePath, "user_" + fileOwner.getId());
        log.debug("User directory: {}", userDir.toString());

        File fileEntity = File.builder()
                .fileName(filename)
                .user(fileOwner)
                .fileLocation(userDir.toString())
                .size(multipartFile.getSize())
                .createdAt(LocalDateTime.now())
                .build();

        fileRepository.save(fileEntity);
        log.debug("File Entity save in DB: {}", fileEntity.toString());

        Files.createDirectories(userDir);
        log.debug("User Directory created");

        Path filePath = userDir.resolve(filename);
        log.debug("File path: {}", filePath.toString());

        multipartFile.transferTo(filePath.toFile());
        log.debug("File saved to Storage");
    }

    @Transactional
    public void deleteFileForUser(String username, String filename) throws IOException {
        log.debug("Starting file delete for user: {}, filename: {}", username, filename);
        User owner = userService.getUserByUsername(username);
        File file = getFileFromDb(filename, owner);

        fileRepository.delete(file);
        log.info("File '{}' deleted from database for user '{}'", filename, username);

        Path filePath = Paths.get(file.getFileLocation(), file.getFileName());
        log.debug("Attempting to delete file from storage: {}", filePath.toString());

        Files.delete(filePath);
        log.info("File '{}' successfully deleted from storage", filename);
    }

    public Resource getFileForUser(String username, String filename) throws IOException {
        User owner = userService.getUserByUsername(username);
        File fileEntity = getFileFromDb(filename, owner);

        Path filePath = Paths.get(fileEntity.getFileLocation(), fileEntity.getFileName());

        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            log.error("Error reading file '{}' from Storage", filename);
            throw new FileStorageException("Error reading file in Storage");
        }

        return resource;
    }

    @Transactional
    public void renameFileForUser(String username, String oldFilename, String newFilename) throws IOException {
        User owner = userService.getUserByUsername(username);
        File fileEntity = getFileFromDb(oldFilename, owner);

        Path oldFilePath = Paths.get(fileEntity.getFileLocation(), fileEntity.getFileName());
        Path newFilePath = Paths.get(fileEntity.getFileLocation(), newFilename);

        Files.move(oldFilePath, newFilePath);

        fileEntity.setFileName(newFilename);
        fileRepository.save(fileEntity);

        log.info("File '{}' renamed to '{}' for user '{}'", oldFilename, newFilename, username);
    }

    public String getFileContentType(Resource resource) throws IOException {
        String contentType = Files.probeContentType(resource.getFile().toPath());
        return (contentType != null) ? contentType : "application/octet-stream";
    }

    public List<FileInfoDto> getUserFilesList(String username, Integer limit) {
        if (limit == null) {
            throw new IllegalArgumentException("Limit parameter is required and cannot be null.");
        }
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be greater than 0.");
        }

        User owner = userService.getUserByUsername(username);

        return fileRepository.findFilesByUserId(owner.getId(), PageRequest.of(0, limit))
                .stream()
                .map(file -> new FileInfoDto(file.getFileName(), file.getSize()))
                .toList();
    }

    private File getFileFromDb(String filename, User owner) {

        return fileRepository.findByFileNameAndUserId(filename, owner.getId())
                .orElseThrow(() -> {
                    log.warn("File '{}' not found in DB for user '{}'", filename, owner.getLogin());
                    return new FileStorageException("File not found in DB");
                });
    }
}
