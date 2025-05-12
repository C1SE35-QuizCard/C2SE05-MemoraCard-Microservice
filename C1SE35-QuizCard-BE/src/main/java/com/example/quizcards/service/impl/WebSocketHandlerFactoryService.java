package com.example.quizcards.service.impl;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;
import org.springframework.web.socket.handler.WebSocketHandlerDecoratorFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketHandlerFactoryService implements WebSocketHandlerDecoratorFactory {
    // Map lưu trữ sessionId -> WebSocketSession
    private static final Map<String, WebSocketSession> sessionMap = new ConcurrentHashMap<>();

    public static WebSocketSession getSession(String sessionId) {
        return sessionMap.get(sessionId);
    }

    @Override
    public WebSocketHandler decorate(WebSocketHandler handler) {
        return new WebSocketHandlerDecorator(handler) {

            @Override
            public void afterConnectionEstablished(WebSocketSession session) throws Exception {
                // Lưu session
                sessionMap.put(session.getId(), session);
                super.afterConnectionEstablished(session);
            }

            @Override
            public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
                // Xoá session khi đóng
                sessionMap.remove(session.getId());
                super.afterConnectionClosed(session, closeStatus);
            }
        };
    }

    // Hàm tiện ích đóng kết nối
    public static void closeSession(String sessionId) throws IOException {
        WebSocketSession session = sessionMap.get(sessionId);
        if (session != null && session.isOpen()) {
            session.close(CloseStatus.NORMAL);
        }
    }
}
