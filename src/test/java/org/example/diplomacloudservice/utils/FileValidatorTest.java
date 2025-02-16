package org.example.diplomacloudservice.utils;

import org.example.diplomacloudservice.exceptions.InvalidFileException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @Test
 * void название-тестируемого-метода_что-проверяет-тест() {...}
 */
class FileValidatorTest {

    @Mock
    private MultipartFile file;

    @BeforeEach
    void setUp() {
        file = mock(MultipartFile.class);
    }

    @Test
    void shouldThrowExceptionWhenFilenameIsEmpty() {
        String filename = "";

        InvalidFileException exception = assertThrows(InvalidFileException.class, () -> {
            FileValidator.validateFilename(filename);
        });

        assertEquals("Filename cannot be empty.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenFilenameIsBlank() {
        String filename = "   ";

        InvalidFileException exception = assertThrows(InvalidFileException.class, () -> {
            FileValidator.validateFilename(filename);
        });

        assertEquals("Filename cannot be empty.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenFilenameHasInvalidCharacters() {
        String filename = "invalid@file.txt";

        InvalidFileException exception = assertThrows(InvalidFileException.class, () -> {
            FileValidator.validateFilename(filename);
        });

        assertEquals("The filename contains invalid characters. Allowed characters are letters (a-z, A-Z), digits (0-9), periods (.), underscores (_), and hyphens (-). Example valid filenames: 'file_name', 'username123', 'file.name', 'a-b_c.d'", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenFilenameHasInvalidExtension() {
        String filename = "file.invalid_extension";

        InvalidFileException exception = assertThrows(InvalidFileException.class, () -> {
            FileValidator.validateFilename(filename);
        });

        assertEquals("Invalid extension format in filename. Only 3 or 4 characters allowed", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenFileIsEmpty() {
        String filename = "validfile.txt";
        when(file.isEmpty()).thenReturn(true);

        InvalidFileException exception = assertThrows(InvalidFileException.class, () -> {
            FileValidator.validateFile(filename, file);
        });

        assertEquals("There was no file attached", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenFileTypeIsBlocked() {
        String filename = "blockedfile.exe";
        when(file.getOriginalFilename()).thenReturn(filename);
        when(file.isEmpty()).thenReturn(false);

        InvalidFileException exception = assertThrows(InvalidFileException.class, () -> {
            FileValidator.validateFile(filename, file);
        });

        assertEquals("This file type is not allowed or file has no extension.", exception.getMessage());
    }

    @Test
    void shouldPassForValidFile() {
        String filename = "validfile.txt";
        when(file.getOriginalFilename()).thenReturn(filename);
        when(file.isEmpty()).thenReturn(false);

        assertDoesNotThrow(() -> FileValidator.validateFile(filename, file));
    }

    @Test
    void shouldThrowExceptionWhenFileHasNoExtension() {
        String filename = "filewithoutextension";
        when(file.getOriginalFilename()).thenReturn(filename);
        when(file.isEmpty()).thenReturn(false);

        InvalidFileException exception = assertThrows(InvalidFileException.class, () -> {
            FileValidator.validateFile(filename, file);
        });

        assertEquals("Invalid extension format in filename. Only 3 or 4 characters allowed", exception.getMessage());
    }

    @Test
    void shouldAllowValidFileExtension() {
        String validFilename = "file123.txt";

        assertDoesNotThrow(() -> FileValidator.validateFilename(validFilename));
    }

    @Test
    void shouldThrowExceptionForInvalidFileExtension() {
        String invalidFilename = "file123.exe";

        InvalidFileException exception = assertThrows(InvalidFileException.class, () -> {
            FileValidator.validateFilename(invalidFilename);
        });

        assertEquals("This file type is not allowed or file has no extension.", exception.getMessage());
    }
}