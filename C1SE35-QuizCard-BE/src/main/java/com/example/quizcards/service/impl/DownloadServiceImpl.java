package com.example.quizcards.service.impl;

import com.example.quizcards.dto.FlashCardDownloadDTO;
import com.example.quizcards.dto.SetDownloadProjectionDTO;
import com.example.quizcards.dto.IFlashCardDownloadDTO;
import com.example.quizcards.repository.IDownloadRepository;
import com.example.quizcards.dto.response.SetDownloadResponse;
import com.example.quizcards.service.IDownloadService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

import static java.time.LocalDateTime.now;

@Service
public class DownloadServiceImpl implements IDownloadService {

    @Autowired
    private IDownloadRepository downloadRepository;

    @Value("${file.storage.path}")
    private String fileStoragePath;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public SetDownloadResponse downloadFlashCard(Long setId) {
        SetDownloadProjectionDTO setInfo = downloadRepository.findSetInfoById(setId);
        if (setInfo == null) {
            return null;
        }

        // Truy vấn danh sách flashcards dưới dạng FlashCardDownloadDTO thay vì IFlashCardDownloadDTO
        List<FlashCardDownloadDTO> flashcards = downloadRepository.findFlashcardsBySetId(setId)
                .stream()
                .map(result -> new FlashCardDownloadDTO(
                        result.getCardId(),
                        result.getQuestion(),
                        result.getAnswer(),
                        result.getImageUrl()
                ))
                .collect(Collectors.toList());

        return new SetDownloadResponse(
                setInfo.getSetId(),
                setInfo.getTitle(),
                setInfo.getDescriptionSet(),
                setInfo.getTotalCard(),
                now(),
                flashcards
        );
    }


    @Override
    public List<Map<String, Object>> getDownloadedFilesWithContent() {
        List<Map<String, Object>> fileList = new ArrayList<>();

        // Lấy danh sách file trong thư mục
        File folder = new File(fileStoragePath);
        File[] files = folder.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".json")) {
                    try {
                        // Đọc nội dung file
                        String rawContent = Files.readString(file.toPath());

                        // Parse chuỗi JSON thành Map
                        Map<String, Object> parsedContent = objectMapper.readValue(rawContent, Map.class);

                        // Chỉ lấy thông tin cần thiết từ content
                        Map<String, Object> minimalContent = new HashMap<>();
                        minimalContent.put("setId", parsedContent.get("setId"));
                        minimalContent.put("title", parsedContent.get("title"));
                        minimalContent.put("descriptionSet", parsedContent.get("descriptionSet"));
                        minimalContent.put("totalCard", parsedContent.get("totalCard"));

                        // Thêm thông tin file vào danh sách
                        Map<String, Object> fileData = new HashMap<>();
                        fileData.put("fileName", file.getName());
                        fileData.put("content", minimalContent); // Đưa nội dung tối giản vào
                        fileList.add(fileData);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return fileList;
    }

    @Override
    public Map<String, Object> getFileContentByName(String fileName) {
        File file = new File(fileStoragePath, fileName);

        if (file.exists() && file.isFile() && file.getName().endsWith(".json")) {
            try {
                // Đọc nội dung file
                String rawContent = Files.readString(file.toPath());

                // Parse chuỗi JSON thành Object
                return objectMapper.readValue(rawContent, Map.class);

            } catch (IOException e) {
                // Ném lỗi ra ngoài với thông tin chi tiết
                throw new RuntimeException("Unable to read file: " + fileName, e);
            }
        } else {
            throw new RuntimeException("File not found: " + fileName);
        }
    }


    // Phương thức để xóa file từ hệ thống
    @Override
    public boolean deleteFile(String fileName) {
        File file = new File(fileStoragePath + "/" + fileName);
        if (file.exists() && file.isFile()) {
            return file.delete(); // Trả về true nếu xóa thành công
        }
        return false; // Trả về false nếu file không tồn tại hoặc không phải là file
    }

}
