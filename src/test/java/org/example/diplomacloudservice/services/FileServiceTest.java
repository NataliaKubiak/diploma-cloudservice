package org.example.diplomacloudservice.services;

import org.example.diplomacloudservice.dto.FileInfoDto;
import org.example.diplomacloudservice.entities.File;
import org.example.diplomacloudservice.entities.User;
import org.example.diplomacloudservice.exceptions.FileStorageException;
import org.example.diplomacloudservice.repositories.FileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock
    private FileRepository fileRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private FileService fileService;

    @Mock
    private MultipartFile multipartFile;

    private static final String USERNAME = "testUser";
    private static final String FILENAME = "testFile.txt";
    private static final String INVALID_FILENAME = "invalidFile.txt";
    private static final Integer USER_ID = 1;
    private static final String FILE_STORAGE_PATH = "storage";

    private User mockUser;
    private File mockFile;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(USER_ID);
        mockUser.setLogin(USERNAME);

        mockFile = new File();
        mockFile.setFileName(FILENAME);
        mockFile.setUser(mockUser);
        mockFile.setFileLocation(FILE_STORAGE_PATH);

        fileService = new FileService(fileRepository, userService);
        fileService.storagePath = FILE_STORAGE_PATH;
    }

    @Test
    void shouldReturnTrueIfFileExistsForUser() {
        when(userService.getUserByUsername(USERNAME)).thenReturn(mockUser);
        when(fileRepository.findByFileNameAndUserId(FILENAME, USER_ID)).thenReturn(Optional.of(mockFile));

        boolean result = fileService.fileExistsForUser(FILENAME, USERNAME);

        assertTrue(result);

        verify(fileRepository, times(1)).findByFileNameAndUserId(FILENAME, USER_ID);
    }

    @Test
    void shouldReturnFalseIfFileDoesNotExistForUser() {
        when(userService.getUserByUsername(USERNAME)).thenReturn(mockUser);
        when(fileRepository.findByFileNameAndUserId(INVALID_FILENAME, USER_ID)).thenReturn(Optional.empty());

        boolean result = fileService.fileExistsForUser(INVALID_FILENAME, USERNAME);

        assertFalse(result);

        verify(fileRepository, times(1)).findByFileNameAndUserId(INVALID_FILENAME, USER_ID);
    }

    @Test
    void shouldUploadFileForUser() throws IOException {
        when(userService.getUserByUsername(USERNAME)).thenReturn(mockUser);
        when(multipartFile.getSize()).thenReturn(10L); // Эмулируем размер файла

        Path userDir = Paths.get(FILE_STORAGE_PATH, "user_" + USER_ID);
        Path filePath = userDir.resolve(FILENAME);

        doNothing().when(multipartFile).transferTo(filePath.toFile());

        fileService.uploadFileForUser(USERNAME, FILENAME, multipartFile);

        verify(fileRepository, times(1)).save(any(File.class));
        verify(multipartFile, times(1)).transferTo(filePath.toFile());

        verify(userService, times(1)).getUserByUsername(USERNAME);
        verify(multipartFile, times(1)).getSize();
    }


    @Test
    void shouldDeleteFileForUser() throws IOException {
        when(userService.getUserByUsername(USERNAME)).thenReturn(mockUser);
        when(fileRepository.findByFileNameAndUserId(FILENAME, USER_ID)).thenReturn(Optional.of(mockFile));
        doNothing().when(fileRepository).delete(mockFile);

        Path filePath = Paths.get(FILE_STORAGE_PATH, FILENAME);

        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.delete(filePath)).thenAnswer(invocation -> null);

            fileService.deleteFileForUser(USERNAME, FILENAME);

            verify(fileRepository, times(1)).delete(mockFile);
            mockedFiles.verify(() -> Files.delete(filePath), times(1));
        }
    }

    @Test
    void shouldThrowExceptionWhenFileNotFoundInDb() {
        when(userService.getUserByUsername(USERNAME)).thenReturn(mockUser);
        when(fileRepository.findByFileNameAndUserId(FILENAME, USER_ID)).thenReturn(Optional.empty());

        assertThrows(FileStorageException.class, () -> fileService.deleteFileForUser(USERNAME, FILENAME));
    }

    @Test
    void shouldThrowIOExceptionWhenFileCannotBeDeletedFromStorage() throws IOException {
        when(userService.getUserByUsername(USERNAME)).thenReturn(mockUser);
        when(fileRepository.findByFileNameAndUserId(FILENAME, USER_ID)).thenReturn(Optional.of(mockFile));
        doNothing().when(fileRepository).delete(mockFile);

        Path filePath = Paths.get(FILE_STORAGE_PATH, FILENAME);

        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.delete(filePath)).thenThrow(IOException.class);

            assertThrows(IOException.class, () -> fileService.deleteFileForUser(USERNAME, FILENAME));
        }
    }

    @Test
    void shouldReturnFileResourceWhenFileExistsAndIsReadable() throws IOException {
        when(userService.getUserByUsername(USERNAME)).thenReturn(mockUser);
        when(fileRepository.findByFileNameAndUserId(FILENAME, USER_ID)).thenReturn(Optional.of(mockFile));

        try (MockedConstruction<UrlResource> mockedConstruction = mockConstruction(UrlResource.class,
                (mockResource, context) -> {
                    when(mockResource.exists()).thenReturn(true);
                    when(mockResource.isReadable()).thenReturn(true);
                })) {

            Resource returnedResource = fileService.getFileForUser(USERNAME, FILENAME);

            assertNotNull(returnedResource);
            assertEquals(mockedConstruction.constructed().get(0), returnedResource);

            UrlResource createdMockResource = mockedConstruction.constructed().get(0);
            verify(createdMockResource).exists();
            verify(createdMockResource).isReadable();
        }
    }

    @Test
    void shouldThrowFileStorageExceptionWhenFileNotReadable() throws IOException {
        when(userService.getUserByUsername(USERNAME)).thenReturn(mockUser);
        when(fileRepository.findByFileNameAndUserId(FILENAME, USER_ID)).thenReturn(Optional.of(mockFile));

        try (MockedConstruction<UrlResource> mockedConstruction = mockConstruction(UrlResource.class,
                (mockResource, context) -> {
                    when(mockResource.exists()).thenReturn(true);
                    when(mockResource.isReadable()).thenReturn(false);
                })) {

            FileStorageException thrown = assertThrows(FileStorageException.class,
                    () -> fileService.getFileForUser(USERNAME, FILENAME));

            assertEquals("Error reading file in Storage", thrown.getMessage());

            UrlResource createdMockResource = mockedConstruction.constructed().get(0);
            verify(createdMockResource).exists();
            verify(createdMockResource).isReadable();
        }
    }

    @Test
    void shouldRenameFileSuccessfully() throws IOException {
        String newFilename = "renamedFile.txt";

        when(userService.getUserByUsername(USERNAME)).thenReturn(mockUser);
        when(fileRepository.findByFileNameAndUserId(FILENAME, USER_ID)).thenReturn(Optional.of(mockFile));

        Path oldFilePath = Paths.get(FILE_STORAGE_PATH, FILENAME);
        Path newFilePath = Paths.get(FILE_STORAGE_PATH, newFilename);

        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            fileService.renameFileForUser(USERNAME, FILENAME, newFilename);

            mockedFiles.verify(() -> Files.move(oldFilePath, newFilePath));
            assertEquals(newFilename, mockFile.getFileName());
            verify(fileRepository).save(mockFile);
        }
    }

    @Test
    void shouldThrowExceptionWhenFileNotFound() {
        String newFilename = "renamedFile.txt";

        when(userService.getUserByUsername(USERNAME)).thenReturn(mockUser);
        when(fileRepository.findByFileNameAndUserId(FILENAME, USER_ID)).thenReturn(Optional.empty());

        assertThrows(FileStorageException.class,
                () -> fileService.renameFileForUser(USERNAME, FILENAME, newFilename));
        verify(fileRepository, never()).save(any());
    }

    @Test
    void shouldThrowIOExceptionWhenFileMoveFails() throws IOException {
        String newFilename = "renamedFile.txt";

        when(userService.getUserByUsername(USERNAME)).thenReturn(mockUser);
        when(fileRepository.findByFileNameAndUserId(FILENAME, USER_ID)).thenReturn(Optional.of(mockFile));

        Path oldFilePath = Paths.get(FILE_STORAGE_PATH, FILENAME);
        Path newFilePath = Paths.get(FILE_STORAGE_PATH, newFilename);

        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.move(oldFilePath, newFilePath)).thenThrow(new IOException("Move failed"));

            assertThrows(IOException.class, () -> fileService.renameFileForUser(USERNAME, FILENAME, newFilename));
            assertEquals(FILENAME, mockFile.getFileName());

            verify(fileRepository, never()).save(any());
        }
    }

    @Test
    void shouldReturnListOfUserFiles() {
        int limit = 2;
        List<File> mockFiles = List.of(
                File.builder()
                        .fileName(FILENAME)
                        .size(1024L)
                        .user(mockUser)
                        .fileLocation(FILE_STORAGE_PATH)
                        .createdAt(LocalDateTime.now()) // Поле обязательно
                        .build(),
                File.builder()
                        .fileName("anotherFile.txt")
                        .size(2048L)
                        .user(mockUser)
                        .fileLocation(FILE_STORAGE_PATH)
                        .createdAt(LocalDateTime.now()) // Поле обязательно
                        .build()
        );

        when(userService.getUserByUsername(USERNAME)).thenReturn(mockUser);
        when(fileRepository.findFilesByUserId(USER_ID, PageRequest.of(0, limit))).thenReturn(mockFiles);

        List<FileInfoDto> fileList = fileService.getUserFilesList(USERNAME, limit);

        assertEquals(2, fileList.size());
        assertEquals(FILENAME, fileList.get(0).getFilename());
        assertEquals(1024L, fileList.get(0).getSize());
        assertEquals("anotherFile.txt", fileList.get(1).getFilename());
        assertEquals(2048L, fileList.get(1).getSize());
    }

    @Test
    void shouldThrowExceptionWhenLimitIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> fileService.getUserFilesList(USERNAME, null));
    }

    @Test
    void shouldThrowExceptionWhenLimitIsZeroOrNegative() {
        assertThrows(IllegalArgumentException.class,
                () -> fileService.getUserFilesList(USERNAME, 0));

        assertThrows(IllegalArgumentException.class,
                () -> fileService.getUserFilesList(USERNAME, -5));
    }
}