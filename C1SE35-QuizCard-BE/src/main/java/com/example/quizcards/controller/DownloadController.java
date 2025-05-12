package com.example.quizcards.controller;

import com.example.quizcards.dto.FlashCardDownloadDTO;
import com.example.quizcards.dto.IFlashCardDownloadDTO;
import com.example.quizcards.dto.response.SetDownloadResponse;
import com.example.quizcards.service.IDownloadService;
import com.example.quizcards.utils.LimitedInputStream;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
@RestController
@RequestMapping("/v1/download")
public class DownloadController {

    @Autowired
    private IDownloadService downloadService;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${file.storage.path}")
    private String fileStoragePath;

    @SneakyThrows
    @GetMapping("/{set_id}")
    public ResponseEntity<Object> downloadFlashcardSet(@PathVariable("set_id") Long setId,
                                                       @RequestHeader(value = "Range", required = false) String rangeHeader) throws IOException {
        // Lấy flashcard set từ service
        SetDownloadResponse flashcards = downloadService.downloadFlashCard(setId);
        if (flashcards == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Flashcard set not found");
        }

        // Tạo thư mục lưu hình ảnh
        String imagesFolderPath = fileStoragePath + "/images/" + flashcards.getSetId();
        File imagesFolder = new File(imagesFolderPath);
        if (!imagesFolder.exists()) {
            imagesFolder.mkdirs(); // Tạo thư mục nếu chưa tồn tại
        }

        // Tải hình ảnh và cập nhật đường dẫn
        for (IFlashCardDownloadDTO flashcard : flashcards.getFlashcards()) {
            String imageUrl = flashcard.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                String imageName = flashcard.getCardId() + ".png"; // Tạo tên file cho hình ảnh
                File imageFile = new File(imagesFolder, imageName);
                try (FileOutputStream fos = new FileOutputStream(imageFile)) {
                    fos.write(new URL(imageUrl).openStream().readAllBytes()); // Tải hình ảnh về
                }
                // Cập nhật đường dẫn hình ảnh trong flashcard
                ((FlashCardDownloadDTO)flashcard).setImageUrl(fileStoragePath + "/images/" + flashcards.getSetId() + "/" + imageName);
            }
        }

        // Lấy tiêu đề của set để đặt làm tên file
        String title = flashcards.getTitle();
        String fileName = title + ".json";

        // Encode tên file theo RFC 5987 để hỗ trợ unicode
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                .replace("+", " ")       // Thay thế + bằng khoảng trắng
                .replace("%2C", ",")     // Giữ lại các ký tự đặc biệt
                .replace("%21", "!")
                .replace("%27", "'")
                .replace("%28", "(")
                .replace("%29", ")")
                .replace("%7E", "~");

        // Chuyển đổi set flashcard thành JSON
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        objectMapper.writeValue(baos, flashcards);
        byte[] data = baos.toByteArray();
        long fileSize = data.length;

        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(data));

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
        headers.add(HttpHeaders.ACCEPT_RANGES, "bytes");

        // Sử dụng header Content-Disposition với tên file đã mã hóa
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName);

        if (rangeHeader == null) {
            // Trả về toàn bộ file
            headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileSize));
            return new ResponseEntity<>(resource, headers, HttpStatus.OK);
        }

        // Xử lý Range Request
        String[] ranges = rangeHeader.replace("bytes=", "").split("-");
        long start = Long.parseLong(ranges[0]);
        long end = ranges.length > 1 && !ranges[1].isEmpty() ? Long.parseLong(ranges[1]) : fileSize - 1;

        if (end >= fileSize) {
            end = fileSize - 1;
        }

        long contentLength = end - start + 1;
        InputStream limitedInputStream = new LimitedInputStream(new ByteArrayInputStream(data), start, contentLength);

        headers.add(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + fileSize);
        headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(contentLength));

        return new ResponseEntity<>(new InputStreamResource(limitedInputStream), headers, HttpStatus.PARTIAL_CONTENT);
    }

    @GetMapping("/downloaded")
    public ResponseEntity<List<Map<String, Object>>> getDownloadedFilesWithContent() {
        return ResponseEntity.ok(downloadService.getDownloadedFilesWithContent());
    }

    // Lấy thông tin chi tiết của một file theo tên
    @GetMapping("/content/{fileName}")
    public ResponseEntity<Map<String, Object>> getFileContentByName(@PathVariable String fileName) {
        try {
            Map<String, Object> fileContent = downloadService.getFileContentByName(fileName);
            return ResponseEntity.ok(fileContent); // Trả về nội dung file nếu thành công
        } catch (RuntimeException e) {
            // Xử lý lỗi khi không tìm thấy file hoặc không đọc được file
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage()); // Trả về thông báo lỗi chi tiết
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    // API xóa file theo tên
    @DeleteMapping("/delete/{fileName}")
    public ResponseEntity<String> deleteFile(@PathVariable("fileName") String fileName) {
        boolean isDeleted = downloadService.deleteFile(fileName);
        if (isDeleted) {
            return ResponseEntity.ok("File deleted successfully");
        } else {
            return ResponseEntity.status(404).body("File not found or could not be deleted");
        }
    }
}
