package org.example.diplomacloudservice.exceptions;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorResponse {

    @JsonProperty("message")
    private String message;

    @JsonProperty("id")
    private int id;

    public ErrorResponse(String message, int id) {
        this.message = message;
        this.id = id;
    }
}
