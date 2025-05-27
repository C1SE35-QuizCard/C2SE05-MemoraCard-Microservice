package com.microservices.videoservice.controller;


import com.microservices.videoservice.dto.UploadResponse;
import com.microservices.videoservice.exception.PartialUploadException;
import com.microservices.videoservice.service.S3VideoService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VideoUploadController {
    S3VideoService s3VideoService;

    //    @PreAuthorize("hasAnyRole('ROLE_PREMIUM_USER')")
//    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    NO CODE: public Mono<ResponseEntity<Map<String,Object>>> upload(@RequestPart("files") Flux<FilePart> files
//
//    ) {
//        return files.hasElements()
//                .flatMap(has -> {
//                    if (!has) {
//                        return Mono.just(ResponseEntity
//                                .badRequest()
//                                .body(UploadResponse.createResponse(false, "No files provided"))  // :contentReference[oaicite:8]{index=8}
//                        );
//                    }
//                    long start = System.currentTimeMillis();
//                    return s3VideoService.uploadVideos(files)
//                            .collectList()
//                            .map(urls -> {
//                                long duration = System.currentTimeMillis() - start;
//                                Map<String,Object> resp = UploadResponse.createResponse(true,
//                                        urls.size()==1 ? "Upload thành công" : "Upload nhiều videos thành công");
//                                resp.put("totalFiles", urls.size());
//                                resp.put("videoUrls", urls);
//                                resp.put("uploadDuration", duration + "ms");
//                                if (urls.size()==1) resp.put("videoUrl", urls.getFirst());
//                                return ResponseEntity.ok(resp);
//                            });
//                })
//                // Bắt lỗi từ service
//                .onErrorResume(PartialUploadException.class, ex -> {
//                    Map<String,Object> resp = UploadResponse.createResponse(false, ex.getMessage());
//                    resp.put("successfulUploads", ex.getSuccessCount());
//                    resp.put("failedUploads", ex.getFailedCount());
//                    resp.put("failedFiles", ex.getFailedFiles());
//                    if (!ex.getSuccessfulUrls().isEmpty()) {
//                        resp.put("videoUrls", ex.getSuccessfulUrls());
//                        if (ex.getSuccessfulUrls().size()==1) resp.put("videoUrl", ex.getSuccessfulUrls().getFirst());
//                    }
//                    return Mono.just(ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(resp));
//                })
//                .onErrorResume(IllegalArgumentException.class, ex ->
//                        Mono.just(ResponseEntity.badRequest()
//                                .body(UploadResponse.createResponse(false, ex.getMessage()))
//                        )
//                )
//                .onErrorResume(ex ->
//                        Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                                .body(UploadResponse.createResponse(false, "Upload failed: " + ex.getMessage()))
//                        )
//                );
//    }

    @PreAuthorize("hasAnyRole('ROLE_PREMIUM_USER')")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<Map<String, Object>>> upload(@RequestPart("files") Flux<FilePart> files,
                                                            @RequestPart("sizes") Flux<String> sizeStrings
    ) {
        Flux<Long> sizes = sizeStrings
                .map(Long::valueOf)
                .onErrorMap(e ->
                        new IllegalArgumentException("Invalid size format", e)
                );

        long start = System.currentTimeMillis();
        return s3VideoService.uploadVideos(files, sizes)
                .collectList()
                .map(urls -> {
                    long duration = System.currentTimeMillis() - start;
                    Map<String, Object> resp = UploadResponse.createResponse(true,
                            urls.size() == 1 ? "Upload thành công" : "Upload nhiều videos thành công");
                    resp.put("totalFiles", urls.size());
                    resp.put("videoUrls", urls);
                    resp.put("uploadDuration", duration + "ms");
                    if (urls.size() == 1) resp.put("videoUrl", urls.getFirst());
                    return ResponseEntity.ok(resp);
                })
                // Bắt lỗi từ service
                .onErrorResume(PartialUploadException.class, ex -> {
                    Map<String, Object> resp = UploadResponse.createResponse(false, ex.getMessage());
                    resp.put("successfulUploads", ex.getSuccessCount());
                    resp.put("failedUploads", ex.getFailedCount());
                    resp.put("failedFiles", ex.getFailedFiles());
                    if (!ex.getSuccessfulUrls().isEmpty()) {
                        resp.put("videoUrls", ex.getSuccessfulUrls());
                        if (ex.getSuccessfulUrls().size() == 1) resp.put("videoUrl", ex.getSuccessfulUrls().getFirst());
                    }
                    return Mono.just(ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(resp));
                })
                .onErrorResume(IllegalArgumentException.class, ex ->
                        Mono.just(ResponseEntity.badRequest()
                                .body(UploadResponse.createResponse(false, ex.getMessage()))
                        )
                )
                .onErrorResume(ex ->
                        Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(UploadResponse.createResponse(false, "Upload failed: " + ex.getMessage()))
                        )
                );
    }

    @PreAuthorize("hasAnyRole('ROLE_PREMIUM_USER')")
    @DeleteMapping("/delete")
    public Mono<ResponseEntity<Map<String, Object>>> delete(@RequestParam("fileName") String fileName) {
        return s3VideoService.deleteVideo(fileName)
                .map(deleted -> {
                    if (!deleted) {
                        return ResponseEntity.ok(
                                UploadResponse.createResponse(false, "Xóa thất bại hoặc file không tồn tại", fileName)
                        );
                    }
                    return ResponseEntity.ok(
                            UploadResponse.createResponse(true, "Xóa thành công", fileName)
                    );
                })
                .onErrorResume(IllegalArgumentException.class, ex ->
                        Mono.just(ResponseEntity.badRequest()
                                .body(UploadResponse.createResponse(false, ex.getMessage()))
                        )
                )
                .onErrorResume(ex ->
                        Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(UploadResponse.createResponse(false, "Delete lỗi: " + ex.getMessage()))
                        )
                );
    }
}
