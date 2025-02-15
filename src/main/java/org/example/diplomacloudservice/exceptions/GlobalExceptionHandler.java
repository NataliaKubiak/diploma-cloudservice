package org.example.diplomacloudservice.exceptions;

import org.example.diplomacloudservice.dto.JsonResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.io.IOException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            BadCredentialsException.class,
            InvalidFileException.class,
            MaxUploadSizeExceededException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<JsonResponse> handleBadCredentialsException(RuntimeException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), 400);
    }

    @ExceptionHandler({
            IOException.class
    })
    public ResponseEntity<JsonResponse> handleIOException() {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "File system access error", 500);
    }

    @ExceptionHandler({
            FileStorageException.class,
            Exception.class
    })
    public ResponseEntity<JsonResponse> handleGeneralException(Exception ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error: " + ex.getMessage(), 500);
    }

    private ResponseEntity<JsonResponse> buildResponse(HttpStatus status, String message, int id) {
        JsonResponse jsonResponse = new JsonResponse(message, id);
        return new ResponseEntity<>(jsonResponse, status);
    }
}
