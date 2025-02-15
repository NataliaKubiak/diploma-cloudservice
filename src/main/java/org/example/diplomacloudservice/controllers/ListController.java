package org.example.diplomacloudservice.controllers;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.diplomacloudservice.dto.FileInfoDto;
import org.example.diplomacloudservice.services.FileService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Log4j2
@RestController
@AllArgsConstructor
public class ListController {

    private final FileService fileService;

    @GetMapping("/list")
    public ResponseEntity<List<FileInfoDto>> showUserFiles(@RequestParam(value = "limit") Integer limit) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        List<FileInfoDto> userFilesList = fileService.getUserFilesList(username, limit);

        return ResponseEntity.ok(userFilesList);
    }
}
