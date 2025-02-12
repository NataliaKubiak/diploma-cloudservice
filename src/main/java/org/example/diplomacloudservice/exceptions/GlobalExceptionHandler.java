package org.example.diplomacloudservice.exceptions;

import lombok.AllArgsConstructor;
import org.example.diplomacloudservice.dto.JsonResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@AllArgsConstructor
public class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<JsonResponse> handleBadCredentialsException(BadCredentialsException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), 400);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<JsonResponse> handleGeneralException(Exception ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error: " + ex.getMessage(), 500);
    }

    private ResponseEntity<JsonResponse> buildResponse(HttpStatus status, String message, int id) {
        JsonResponse jsonResponse = new JsonResponse(message, id);
        return new ResponseEntity<>(jsonResponse, status);
    }
}
