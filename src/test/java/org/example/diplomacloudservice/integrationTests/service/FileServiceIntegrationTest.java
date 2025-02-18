package org.example.diplomacloudservice.integrationTests.service;

import org.example.diplomacloudservice.dto.FileInfoDto;
import org.example.diplomacloudservice.entities.File;
import org.example.diplomacloudservice.entities.User;
import org.example.diplomacloudservice.exceptions.FileStorageException;
import org.example.diplomacloudservice.repositories.FileRepository;
import org.example.diplomacloudservice.repositories.UserRepository;
import org.example.diplomacloudservice.services.FileService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.multipart.MultipartFile;
import org.testcontainers.containers.PostgreSQLContainer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FileServiceIntegrationTest {

    @Autowired
    private FileService fileService;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private UserRepository userRepository;

    @Value("${file.storage.location:storage}")
    String storagePath;

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:16-alpine"
    );

    @BeforeAll
    static void beforeAll() {
        postgres.start();
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    void setUp() {
        fileRepository.deleteAll();
    }

    @Test
    void fileExistsForUser_fileExists() {
        User user = userRepository.findByLogin("user1")
                .orElseThrow(() -> new UsernameNotFoundException("Test User not found"));
        File file = File.builder()
                .fileName("testFile.txt")
                .user(user)
                .fileLocation("")
                .createdAt(LocalDateTime.now())
                .build();

        fileRepository.save(file);

        assertTrue(fileService.fileExistsForUser("testFile.txt", "user1"));
    }

    @Test
    void fileExistsForUser_fileNotExist() {
        assertFalse(fileService.fileExistsForUser("testFile111.txt", "user1"));
    }

    @Test
    void uploadFileForUser_uploadSuccessful() throws IOException {
        User user = userRepository.findByLogin("user1")
                .orElseThrow(() -> new UsernameNotFoundException("Test User not found"));

        String filename = "testUpload.txt";
        byte[] fileContent = "Hello, World!".getBytes();
        MultipartFile multipartFile = new MockMultipartFile(filename, filename, "text/plain", fileContent);

        fileService.uploadFileForUser(user.getLogin(), filename, multipartFile);

        Optional<File> savedFile = fileRepository.findByFileNameAndUserId(filename, user.getId());
        assertTrue(savedFile.isPresent());

        Path userDir = Paths.get(storagePath, "user_" + user.getId());
        Path filePath = userDir.resolve(filename);
        assertTrue(Files.exists(filePath));

        assertEquals(fileContent.length, Files.size(filePath));
    }

    @Test
    void deleteFileForUser_DeleteSuccessful() throws IOException {
        User user = userRepository.findByLogin("user1")
                .orElseThrow(() -> new UsernameNotFoundException("Test User not found"));

        String filename = "testUpload.txt";
        byte[] fileContent = "Hello, World!".getBytes();
        MultipartFile multipartFile = new MockMultipartFile(filename, filename, "text/plain", fileContent);

        fileService.uploadFileForUser(user.getLogin(), filename, multipartFile);

        Optional<File> savedFile = fileRepository.findByFileNameAndUserId(filename, user.getId());
        assertTrue(savedFile.isPresent());

        Path userDir = Paths.get(storagePath, "user_" + user.getId());
        Path filePath = userDir.resolve(filename);
        assertTrue(Files.exists(filePath));

        fileService.deleteFileForUser(user.getLogin(), filename);

        Optional<File> deletedFile = fileRepository.findByFileNameAndUserId(filename, user.getId());
        assertFalse(deletedFile.isPresent());

        assertFalse(Files.exists(filePath));
    }

    @Test
    void getFileForUser_FileExistsAndReadable() throws IOException {
        User user = userRepository.findByLogin("user1")
                .orElseThrow(() -> new UsernameNotFoundException("Test User not found"));

        String filename = "testUpload.txt";
        byte[] fileContent = "Hello, World!".getBytes();
        MultipartFile multipartFile = new MockMultipartFile(filename, filename, "text/plain", fileContent);

        fileService.uploadFileForUser(user.getLogin(), filename, multipartFile);

        Optional<File> savedFile = fileRepository.findByFileNameAndUserId(filename, user.getId());
        assertTrue(savedFile.isPresent());

        Resource resource = fileService.getFileForUser(user.getLogin(), filename);

        assertNotNull(resource);
        assertTrue(resource.exists());
        assertTrue(resource.isReadable());

        byte[] fileBytes = resource.getInputStream().readAllBytes();
        assertArrayEquals(fileContent, fileBytes);
    }

    @Test
    void getFileForUser_FileNotFound() {
        User user = userRepository.findByLogin("user1")
                .orElseThrow(() -> new UsernameNotFoundException("Test User not found"));

        String filename = "nonexistentFile.txt";

        assertThrows(FileStorageException.class, () -> fileService.getFileForUser(user.getLogin(), filename));
    }

    @Test
    void renameFileForUser_renameSuccessful() throws IOException {
        User user = userRepository.findByLogin("user1")
                .orElseThrow(() -> new UsernameNotFoundException("Test User not found"));

        String oldFilename = "testUpload.txt";
        String newFilename = "renamedFile.txt";
        byte[] fileContent = "Hello, World!".getBytes();
        MultipartFile multipartFile = new MockMultipartFile(oldFilename, oldFilename, "text/plain", fileContent);

        fileService.uploadFileForUser(user.getLogin(), oldFilename, multipartFile);

        Optional<File> savedFile = fileRepository.findByFileNameAndUserId(oldFilename, user.getId());
        assertTrue(savedFile.isPresent());

        Path userDir = Paths.get(storagePath, "user_" + user.getId());
        Path oldFilePath = userDir.resolve(oldFilename);

        assertTrue(Files.exists(oldFilePath));

        fileService.renameFileForUser(user.getLogin(), oldFilename, newFilename);

        Optional<File> renamedFile = fileRepository.findByFileNameAndUserId(newFilename, user.getId());
        assertTrue(renamedFile.isPresent());

        Optional<File> oldFileAfterRename = fileRepository.findByFileNameAndUserId(oldFilename, user.getId());
        assertFalse(oldFileAfterRename.isPresent());

        Path newFilePath = userDir.resolve(newFilename);
        assertTrue(Files.exists(newFilePath));
        assertFalse(Files.exists(oldFilePath));

        byte[] fileBytes = Files.readAllBytes(newFilePath);
        assertArrayEquals(fileContent, fileBytes);

        Files.delete(newFilePath);
    }

    @Test
    void getUserFilesList_validInput_returnsFileList() {
        User user = userRepository.findByLogin("user1")
                .orElseThrow(() -> new UsernameNotFoundException("Test User not found"));

        File file1 = File.builder()
                .fileName("file1.txt")
                .user(user)
                .fileLocation("/some/path/to/file1.txt")
                .size(10L)
                .createdAt(LocalDateTime.now())
                .build();

        File file2 = File.builder()
                .fileName("file2.txt")
                .user(user)
                .fileLocation("/some/path/to/file2.txt")
                .size(20L)
                .createdAt(LocalDateTime.now())
                .build();

        fileRepository.save(file1);
        fileRepository.save(file2);

        List<FileInfoDto> fileInfoDtos = fileService.getUserFilesList(user.getLogin(), 2);

        assertFalse(fileInfoDtos.isEmpty());

        assertEquals(2, fileInfoDtos.size());
        assertTrue(fileInfoDtos.stream().anyMatch(file -> "file1.txt".equals(file.getFilename())));
        assertTrue(fileInfoDtos.stream().anyMatch(file -> "file2.txt".equals(file.getFilename())));

        assertTrue(fileInfoDtos.stream().anyMatch(file -> file.getFilename().equals("file1.txt") && file.getSize() == file1.getSize()));
        assertTrue(fileInfoDtos.stream().anyMatch(file -> file.getFilename().equals("file2.txt") && file.getSize() == file2.getSize()));
    }
}
