package org.example.diplomacloudservice.services;

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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        // Мокаем пользователя, возвращаемого сервисом
        when(userService.getUserByUsername(USERNAME)).thenReturn(mockUser);

        // Мокаем файл, найденный в базе данных
        when(fileRepository.findByFileNameAndUserId(FILENAME, USER_ID)).thenReturn(Optional.of(mockFile));

        // Мокаем Path для файла
        Path filePath = Paths.get(FILE_STORAGE_PATH, FILENAME);

        // Используем mockConstruction для мока конструктора UrlResource
        try (MockedConstruction<UrlResource> mockedConstruction = mockConstruction(UrlResource.class,
                (mockResource, context) -> {
                    when(mockResource.exists()).thenReturn(true);
                    when(mockResource.isReadable()).thenReturn(true);
                })) {

            // Вызываем метод
            Resource returnedResource = fileService.getFileForUser(USERNAME, FILENAME);

            // Проверяем, что ресурс был возвращен
            assertNotNull(returnedResource);
            assertEquals(mockedConstruction.constructed().get(0), returnedResource);

            // Проверяем, что resource был проверен на существование и читаемость
            UrlResource createdMockResource = mockedConstruction.constructed().get(0);
            verify(createdMockResource).exists();
            verify(createdMockResource).isReadable();
        }
    }

    @Test
    void shouldThrowFileStorageExceptionWhenFileNotReadable() throws IOException {
        // Мокаем пользователя
        when(userService.getUserByUsername(USERNAME)).thenReturn(mockUser);

        // Мокаем файл в БД
        when(fileRepository.findByFileNameAndUserId(FILENAME, USER_ID)).thenReturn(Optional.of(mockFile));

        // Мокаем Path
        Path filePath = Paths.get(FILE_STORAGE_PATH, FILENAME);

        // Используем mockConstruction для мока конструктора UrlResource
        try (MockedConstruction<UrlResource> mockedConstruction = mockConstruction(UrlResource.class,
                (mockResource, context) -> {
                    when(mockResource.exists()).thenReturn(true);
                    when(mockResource.isReadable()).thenReturn(false);
                })) {

            // Проверяем, что метод выбросит исключение
            FileStorageException thrown = assertThrows(FileStorageException.class,
                    () -> fileService.getFileForUser(USERNAME, FILENAME));

            assertEquals("Error reading file in Storage", thrown.getMessage());

            // Получаем мок-объект из контекста и проверяем вызовы
            UrlResource createdMockResource = mockedConstruction.constructed().get(0);
            verify(createdMockResource).exists();
            verify(createdMockResource).isReadable();
        }
    }
}