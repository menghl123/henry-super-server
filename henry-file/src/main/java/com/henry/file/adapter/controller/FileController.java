package com.henry.file.adapter.controller;

import com.henry.common.result.Result;
import com.henry.file.application.FileApplicationService;
import com.henry.file.application.dto.DownloadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

    private final FileApplicationService fileApplicationService;

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        return Result.success(fileApplicationService.upload(file));
    }

    @GetMapping("/download/{storedName}")
    public ResponseEntity<InputStreamResource> download(@PathVariable String storedName) {
        DownloadResponse response = fileApplicationService.download(storedName);
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(response.getOriginalName(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(response.getContent()));
    }
}
