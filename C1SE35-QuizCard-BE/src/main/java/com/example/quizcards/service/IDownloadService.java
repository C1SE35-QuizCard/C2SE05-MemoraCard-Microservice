package com.example.quizcards.service;

import com.example.quizcards.dto.response.SetDownloadResponse;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface IDownloadService {
    SetDownloadResponse downloadFlashCard(Long setId);

    List<Map<String, Object>> getDownloadedFilesWithContent();

    Map<String, Object> getFileContentByName(String fileName);

    boolean deleteFile(String fileName);
}
