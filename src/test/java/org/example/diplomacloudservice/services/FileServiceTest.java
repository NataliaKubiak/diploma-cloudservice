package org.example.diplomacloudservice.services;

import org.example.diplomacloudservice.entities.File;
import org.example.diplomacloudservice.entities.User;
import org.example.diplomacloudservice.repositories.FileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

    // Когда файл существует для пользователя
    @Test
    void shouldReturnTrueIfFileExistsForUser() {
        when(userService.getUserByUsername(USERNAME)).thenReturn(mockUser);
        when(fileRepository.findByFileNameAndUserId(FILENAME, USER_ID)).thenReturn(Optional.of(mockFile));

        boolean result = fileService.fileExistsForUser(FILENAME, USERNAME);

        assertTrue(result);

        verify(fileRepository, times(1)).findByFileNameAndUserId(FILENAME, USER_ID);
    }

    // Когда файл не существует для пользователя
    @Test
    void shouldReturnFalseIfFileDoesNotExistForUser() {
        when(userService.getUserByUsername(USERNAME)).thenReturn(mockUser);
        when(fileRepository.findByFileNameAndUserId(INVALID_FILENAME, USER_ID)).thenReturn(Optional.empty());

        boolean result = fileService.fileExistsForUser(INVALID_FILENAME, USERNAME);

        assertFalse(result);

        verify(fileRepository, times(1)).findByFileNameAndUserId(INVALID_FILENAME, USER_ID);
    }

    // Когда мы загружаем файл для пользователя
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
}