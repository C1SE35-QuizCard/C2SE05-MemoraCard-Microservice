package com.microservices.videoservice.service;

import com.microservices.videoservice.exception.PartialUploadException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.transfer.s3.S3TransferManager;
import software.amazon.awssdk.transfer.s3.model.UploadRequest;
import software.amazon.awssdk.transfer.s3.progress.LoggingTransferListener;

import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class S3VideoService {
    private final S3AsyncClient s3AsyncClient;    // reactive client
    private final String bucketName;
    private final String region;
    private final S3TransferManager transferManager;

    private static final Set<String> ALLOWED_VIDEO_TYPES = Set.of(
            "video/mp4","video/avi","video/mov","video/wmv",
            "video/flv","video/webm","video/mkv","video/m4v"
    );

    @Autowired
    public S3VideoService(S3AsyncClient s3AsyncClient,
                          S3TransferManager transferManager,
                          @Qualifier("s3BucketName") String bucketName,
                          @Qualifier("s3RegionStatic") String region) {
        this.s3AsyncClient = s3AsyncClient;
        this.transferManager = transferManager;
        this.bucketName = bucketName;
        this.region = region;
    }

    public Flux<String> uploadVideos(Flux<FilePart> files) {
        AtomicInteger idx = new AtomicInteger(1);

        return files
                .flatMap(file -> Mono.defer(() -> {
                    int fileIndex = idx.getAndIncrement();

                    // 1) Validate ngay
                    MediaType ct = file.headers().getContentType();
                    if (ct == null || !ALLOWED_VIDEO_TYPES.contains(ct.toString())) {
                        return Mono.error(new IllegalArgumentException(
                                "File " + fileIndex + " không hợp lệ. Chỉ chấp nhận: " + ALLOWED_VIDEO_TYPES
                        ));
                    }
                    long length = file.headers().getContentLength();
                    if (length > 100 * 1024 * 1024) {
                        return Mono.error(new IllegalArgumentException(
                                String.format("File %d vượt quá 100MB", fileIndex)
                        ));
                    }


                    // 2) Sinh key
                    String fileName = Instant.now()
                            .atZone(ZoneOffset.UTC)
                            .toLocalDateTime()
                            .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                            + "_" + UUID.randomUUID().toString().substring(0,8)
                            + "_" + file.filename();

                    String key = "videos/" + fileName;

                    // 3) Build PutObjectRequest (có thể bỏ contentLength; SDK sẽ chunked upload)
                    PutObjectRequest req = PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .contentType(ct.toString())
                            .build();

                    // 4) Stream trực tiếp DataBuffer -> ByteBuffer vào S3
                    Flux<ByteBuffer> publisher = file.content()
                            .map(DataBuffer::asByteBuffer);

                    UploadRequest ulq = UploadRequest.builder()
                            .putObjectRequest(req)
                            .addTransferListener(LoggingTransferListener.create())
                            .requestBody(AsyncRequestBody.fromPublisher(publisher))
                            .build();

                    return Mono.fromCompletionStage(
                                    transferManager.upload(ulq)
                                            .completionFuture()
                            )
                            .map(resp -> "https://" + bucketName + ".s3.amazonaws.com/" + key)
                            .onErrorMap(e -> {
                                log.error("Upload thất bại file {}: {}", file.filename(), e.getMessage());
                                return new PartialUploadException(
                                        "Không thể upload file " + file.filename(),
                                        /*successCount=*/0,
                                        /*failedCount=*/1,
                                        List.of(),
                                        List.of(file.filename())
                                );
                            });
                }))
                // nếu gặp PartialUploadException thì propagate để controller handle partial
                .onErrorResume(PartialUploadException.class, Mono::error)
                // bất kỳ lỗi nào khác gói thành RuntimeException
                .onErrorResume(e ->
                        Mono.error(new RuntimeException("Upload videos lỗi: " + e.getMessage(), e))
                );
    }

    public Flux<String> uploadVideos(Flux<FilePart> files, Flux<Long> sizes) {
        return Flux.zip(files, sizes)
                .flatMap(tuple -> uploadOne(tuple.getT1(), tuple.getT2()));
    }

    public Mono<String> uploadOne(FilePart file, long contentLength) {
        return Mono.defer(() -> {
            String filename = file.filename();
            // 1) Validate contentType
            String ct = Optional.ofNullable(file.headers().getContentType())
                    .map(Object::toString)
                    .orElse("");
            if (!ALLOWED_VIDEO_TYPES.contains(ct)) {
                return Mono.error(new IllegalArgumentException(
                        "File “" + filename + "” không hợp lệ. Kiểu phải là video."
                ));
            }
            // 2) Validate size
            if (contentLength <= 0) {
                return Mono.error(new IllegalArgumentException(
                        "Không xác định được kích thước file “" + filename + "”."
                ));
            }
            if (contentLength > 100L * 1024 * 1024) {
                return Mono.error(new IllegalArgumentException(
                        String.format("File “%s” vượt quá 100MB.", filename)
                ));
            }
            // 3) Sinh key
//            String timestamp = Instant.now()
//                    .atZone(ZoneOffset.UTC)
//                    .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
//            String safeName = URLEncoder.encode(filename, StandardCharsets.UTF_8);
//            String key = "videos/" + timestamp + "_" +
//                    UUID.randomUUID().toString().substring(0,8) +
//                    "_" + safeName;
            String fileName = Instant.now()
                    .atZone(ZoneOffset.UTC)
                    .toLocalDateTime()
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                    + "_" + UUID.randomUUID().toString().substring(0,8)
                    + "_" + file.filename();

            String key = "videos/" + fileName;

            // 4) Build PutObjectRequest với contentLength
            PutObjectRequest req = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(ct)
                    .contentLength(contentLength)
                    .build();

            // 5) Stream từ FilePart → ByteBuffer → S3
            Flux<ByteBuffer> publisher = file.content()
                    .map(DataBuffer::asByteBuffer);

            return Mono.fromCompletionStage(
                            s3AsyncClient.putObject(req,
                                    AsyncRequestBody.fromPublisher(publisher))
                    )
                    // 6) Trả về URL
                    .map(resp -> "https://" + bucketName + ".s3.amazonaws.com/" + key)
                    // 7) Map lỗi thành PartialUploadException để controller có thể partial-handle
                    .onErrorMap(e -> {
                        log.error("Upload thất bại file {}: {}", filename, e.getMessage());
                        return new PartialUploadException(
                                "Không thể upload “" + filename + "”",
                                /*successCount=*/0,
                                /*failedCount=*/1,
                                /*successfulFiles=*/List.of(),
                                /*failedFiles=*/List.of(filename)
                        );
                    });
        });
    }

    public Mono<Boolean> deleteVideo(String fileName) {
        return Mono.fromCallable(() -> {
                    if (fileName == null || fileName.isBlank()) {
                        throw new IllegalArgumentException("fileName không được để trống");
                    }
                    return fileName;
                })
                .flatMap(fn -> {
                    DeleteObjectRequest req = DeleteObjectRequest.builder()
                            .bucket(bucketName)
                            .key("videos/" + fn)
                            .build();

                    return Mono.fromCompletionStage(s3AsyncClient.deleteObject(req))
                            .thenReturn(true);
                })
                .onErrorResume(e -> {
                    log.error("Xóa video lỗi: {}", e.getMessage());
                    return Mono.just(false);
                });
    }

    public Mono<Boolean> videoExists(String fileName) {
        return Mono.fromCompletionStage(
                        s3AsyncClient.headObject(builder -> builder
                                .bucket(bucketName)
                                .key("videos/" + fileName))
                )
                .map(resp -> true)
                .onErrorResume(NoSuchKeyException.class, ex -> Mono.just(false))
                .onErrorResume(e -> {
                    log.error("Kiểm tra tồn tại lỗi: {}", e.getMessage());
                    return Mono.error(new RuntimeException("Không thể kiểm tra tồn tại: " + fileName, e));
                });
    }
}
