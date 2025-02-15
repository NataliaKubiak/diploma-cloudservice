package org.example.diplomacloudservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class FileInfoDto {

    @JsonProperty("filename")
    private String filename;

    @JsonProperty("size")
    private long size;
}
