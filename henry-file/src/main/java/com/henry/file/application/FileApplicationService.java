package com.henry.file.application;

import com.henry.common.exception.BusinessException;
import com.henry.file.application.dto.DownloadResponse;
import com.henry.file.domain.model.StoredFile;
import com.henry.file.domain.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * 文件应用服务：编排存储与读取
 */
@Service
@RequiredArgsConstructor
public class FileApplicationService {

    private final FileRepository fileRepository;

    public String upload(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        StoredFile storedFile = StoredFile.create(file.getOriginalFilename());
        try (InputStream in = file.getInputStream()) {
            fileRepository.store(storedFile, in);
        } catch (IOException e) {
            throw new BusinessException("文件保存失败");
        }
        return storedFile.getStoredName();
    }

    public DownloadResponse download(String storedName) {
        StoredFile storedFile = StoredFile.fromStoredName(storedName);
        return new DownloadResponse(storedFile.getOriginalName(), fileRepository.load(storedFile));
    }
}
