package com.example.quizcards.helpers.TestHelpers;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

public class TestExecutorSession {
    private static final Map<Long, ScheduledFuture<?>> testExecutor = new ConcurrentHashMap<>();

    public static void shutdownExecutorByTestId(Long testId) {
        ScheduledFuture<?> future = testExecutor.get(testId);
        if (future != null) {
            future.cancel(true);
            testExecutor.remove(testId);
        }
    }

    public static ScheduledFuture<?> getTestExecutorByTestId(Long testId) {
        return testExecutor.get(testId);
    }

    public static void addTestExecutor(Long testId, ScheduledFuture<?> future) {
        if (!testExecutor.containsKey(testId)) {
            testExecutor.put(testId, future);
        }
    }

    public static void removeTestExecutor(Long testId) {
        testExecutor.remove(testId);
    }
}
