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

    public void fileExistsForUser(String filename, String username) {
        User owner = userService.getUserByUsername(username);

        Optional<File> maybeFile = fileRepository.findByFileNameAndUserId(filename, owner.getId());

        if(maybeFile.isPresent()) {
            throw new InvalidFileException("File with name '" + filename + "' already exist");
        }
    }
}
