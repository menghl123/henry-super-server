package com.henry.file.application.service;

import com.henry.file.application.assembler.FileAssembler;
import com.henry.file.application.dto.FileDTO;
import com.henry.file.application.repository.FileRepository;
import com.henry.file.domain.model.FileObject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件应用服务：编排上传、下载、删除领域逻辑。
 * <p>
 * 事务与安全不涉及（纯文件存储，无数据库），业务校验集中在此层。
 */
@Service
@RequiredArgsConstructor
public class FileApplicationService {

    private final FileRepository fileRepository;
    private final FileAssembler fileAssembler;

    /** 单文件上传 */
    public FileDTO upload(final MultipartFile file) {
        return fileAssembler.toDTO(doSave(file));
    }

    /** 批量上传：任一文件为空则整体失败，避免部分成功造成状态不一致 */
    public List<FileDTO> uploadBatch(final List<MultipartFile> files) {
        if (CollectionUtils.isEmpty(files)) {
            throw new IllegalArgumentException("批量上传文件不能为空");
        }
        for (final MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("批量上传中存在空文件");
            }
        }
        return files.stream()
                .map(this::doSave)
                .map(fileAssembler::toDTO)
                .collect(Collectors.toList());
    }

    /** 查询文件元数据（下载前调用） */
    public FileObject getFile(final String id) {
        return fileRepository.find(id)
                .orElseThrow(() -> new IllegalArgumentException("文件不存在"));
    }

    /** 打开文件内容流（调用方负责关闭） */
    public InputStream openStream(final String id) {
        return fileRepository.openStream(id)
                .orElseThrow(() -> new IllegalArgumentException("文件不存在或已被删除"));
    }

    /** 单文件删除 */
    public void delete(final String id) {
        if (!fileRepository.remove(id)) {
            throw new IllegalArgumentException("文件不存在");
        }
    }

    /** 批量删除：先整体校验全部存在，再逐个删除，避免部分删除 */
    public void deleteBatch(final List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new IllegalArgumentException("待删除文件id列表不能为空");
        }
        for (final String id : ids) {
            getFile(id);
        }
        for (final String id : ids) {
            fileRepository.remove(id);
        }
    }

    private FileObject doSave(final MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        try (final InputStream in = file.getInputStream()) {
            return fileRepository.save(in, file.getOriginalFilename(), file.getContentType());
        } catch (final IOException e) {
            throw new IllegalStateException("读取上传文件失败", e);
        }
    }
}
