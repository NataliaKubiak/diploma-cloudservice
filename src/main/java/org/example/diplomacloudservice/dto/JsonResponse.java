package org.example.diplomacloudservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class JsonResponse {

    @JsonProperty("message")
    private String message;

    @JsonProperty("id")
    private int id;
}
