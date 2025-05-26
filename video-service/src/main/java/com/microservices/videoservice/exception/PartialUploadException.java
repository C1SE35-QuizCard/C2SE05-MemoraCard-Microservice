package com.microservices.videoservice.exception;

import java.util.List;


public class PartialUploadException extends Exception {
    private final int successCount;
    private final int failedCount;
    private final List<String> failedFiles;
    private final List<String> successfulUrls;

    public PartialUploadException(String message, int successCount, int failedCount,
                                  List<String> failedFiles, List<String> successfulUrls) {
        super(message);
        this.successCount = successCount;
        this.failedCount = failedCount;
        this.failedFiles = failedFiles;
        this.successfulUrls = successfulUrls;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public int getFailedCount() {
        return failedCount;
    }

    public List<String> getFailedFiles() {
        return failedFiles;
    }

    public List<String> getSuccessfulUrls() {
        return successfulUrls;
    }
}
