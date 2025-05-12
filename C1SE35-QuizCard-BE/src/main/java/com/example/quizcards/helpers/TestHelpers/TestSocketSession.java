package com.example.quizcards.helpers.TestHelpers;

import com.example.quizcards.service.impl.WebSocketHandlerFactoryService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class TestSocketSession {
    private static final Map<Long, List<String>> testSessions = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2); // Chỉ 2 luồng cho shutdown

    public static void addNewTestSessions(Long testId, String sessionId) {
        testSessions.computeIfAbsent(testId, k -> new ArrayList<>()).add(sessionId);
    }

    public static void shutdownTest(Long testId) {
        if (testSessions.get(testId) != null) {
            List<String> sessions = new ArrayList<>(testSessions.get(testId));
            for (String sessionId : sessions) {
                try {
                    WebSocketHandlerFactoryService.closeSession(sessionId);
                } catch (Exception e) {
                    //
                }
            }
            testSessions.remove(testId);
        }
    }

    public static void removeSessionTest(Long testId, String sessionId) {
        List<String> sessions = testSessions.get(testId);
        if (sessions != null) {
            sessions.remove(sessionId);
        }
    }

    private static List<String> pollBatch(ConcurrentLinkedQueue<String> queue, int batchSize) {
        List<String> batch = new ArrayList<>(batchSize);
        for (int i = 0; i < batchSize; i++) {
            String session = queue.poll();
            if (session == null) break;
            batch.add(session);
        }
        return batch;
    }
}
