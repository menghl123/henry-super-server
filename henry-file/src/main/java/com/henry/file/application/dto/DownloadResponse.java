package com.henry.file.application.dto;

import lombok.Value;

import java.io.InputStream;

@Value
public class DownloadResponse {

    String originalName;

    InputStream content;
}
