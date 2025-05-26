package com.microservices.videoservice.dto;

import java.util.HashMap;
import java.util.Map;

public class UploadResponse {
    public static Map<String, Object> createResponse(boolean success, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    public static Map<String, Object> createResponse(boolean success, String message, String fileName) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", message);
        response.put("fileName", fileName);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
}