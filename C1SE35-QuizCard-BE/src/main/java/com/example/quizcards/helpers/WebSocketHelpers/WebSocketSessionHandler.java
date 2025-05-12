package com.example.quizcards.helpers.WebSocketHelpers;

import com.example.quizcards.service.impl.WebSocketHandlerFactoryService;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketSessionHandler {
    private final static Map<Long, Map<String, List<String>>> listConnections = new ConcurrentHashMap<>();

    public static void putConnection(Long userId, String deviceId, String sessionId) {
        Objects.requireNonNull(listConnections.putIfAbsent(userId, new HashMap<>()))
                .computeIfAbsent(deviceId, k -> new ArrayList<>())
                .add(sessionId);
    }

    public void clearAllConnectionsByUserId(Long userId) throws IOException {
        if (listConnections.containsKey(userId)) {
            Map<String, List<String>> connections = listConnections.get(userId);
            for (Map.Entry<String, List<String>> entry : connections.entrySet()) {
                for (String sessionId : entry.getValue()) {
                    WebSocketHandlerFactoryService.closeSession(sessionId);
                }
            }
            listConnections.remove(userId);
        }
    }

    public void clearAllConnectionsByDeviceId(String deviceId) throws IOException {
        for (Map.Entry<Long, Map<String, List<String>>> entry : listConnections.entrySet()) {
            if (entry.getValue().containsKey(deviceId)) {
                for (String sessionId : entry.getValue().get(deviceId)) {
                    WebSocketHandlerFactoryService.closeSession(sessionId);
                }
                entry.getValue().remove(deviceId);
            }
        }
    }

    @SneakyThrows
    public void clearAllConnectionsByUserAndDeviceId(Long userId, String deviceId) throws IOException {
        if (listConnections.containsKey(userId)) {
            Map<String, List<String>> connections = listConnections.get(userId);
            if (connections.containsKey(deviceId)) {
                for (String sessionId : connections.get(deviceId)) {
                    try {
                        WebSocketHandlerFactoryService.closeSession(sessionId);
                    } catch (Exception e) {
                        //
                    }
                }
                connections.remove(deviceId);
            }
        }
    }
}
