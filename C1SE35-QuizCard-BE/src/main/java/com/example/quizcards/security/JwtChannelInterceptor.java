package com.example.quizcards.security;

import com.example.quizcards.entities.TestDataPackage.TestData;
import com.example.quizcards.helpers.TestHelpers.TestSocketSession;
import com.example.quizcards.repository.ITestDataMongoDbRepo;
import com.example.quizcards.service.ICustomUserDetailsService;
import com.example.quizcards.utils.global_vars.TestVars;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
public class JwtChannelInterceptor implements ChannelInterceptor {
    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private ICustomUserDetailsService customUserDetailsService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ITestDataMongoDbRepo mongoDbRepo;

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtChannelInterceptor.class);

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            handleConnect(message, accessor);
        }
        if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
            handleDisconnect(message, accessor);
        }
        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            handleSubscribe(message, accessor);
        }
        if (StompCommand.SEND.equals(accessor.getCommand())) {
            //
        }
        return message;
    }

    private void handleConnect(Message<?> message, StompHeaderAccessor accessor) {
        System.out.println(accessor.getSessionId());

        List<String> authorization = accessor.getNativeHeader("Authorization");

        String token = null;

        if (authorization != null && !authorization.isEmpty()) {
            token = authorization.get(0).replace("Bearer ", "");
        }

        if (StringUtils.hasText(token)) {
            try {
                UserDetails userDetails;
                String username = tokenProvider.getUsernameFromJWT(token);

                if (username != null) {
                    userDetails = customUserDetailsService.loadUserByUsernameOnly(username);
                    if (!userDetails.isEnabled()) {
                        throw new RuntimeException("User " + username + " is disabled");
                    }
                    if (accessor.getUser() == null) {
                        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                        accessor.setUser(auth);
                    }
                } else {
                    throw new MessageDeliveryException("Invalid token");
                }
            } catch (Exception ex) {
                LOGGER.error("Could not set user authentication in security context", ex);
            }
        }
    }

    private void handleDisconnect(Message<?> message, StompHeaderAccessor accessor) {
        disconnectToTest(message, accessor);
    }

    private void disconnectToTest(Message<?> message, StompHeaderAccessor accessor) {
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes != null) {
            TestData obj = (TestData) sessionAttributes.get(TestVars.TEST_ACCEPT_INFO);
            if (obj != null) {
                TestSocketSession.removeSessionTest(obj.getTestId(), accessor.getSessionId());
            }
        }
    }

    private void handleSubscribe(Message<?> message, StompHeaderAccessor accessor) {
        testNotifyRegisterSubscribe(message, accessor);
    }

    private void testNotifyRegisterSubscribe(Message<?> message, StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        // kiểm tra nếu có đuôi là:
        if (destination != null && destination.startsWith("/topic/test/register/")) {
            // Tách testId ra
            String testIdStr = destination.substring("/topic/test/register/".length());

            try {
                Long testId = Long.parseLong(testIdStr);
                // Lấy user từ accessor
                Principal iUser = accessor.getUser();
                if (iUser == null) {
                    // Chưa đăng nhập, không cho subscribe
                    throw new SecurityException("User not authenticated");
                }

                Authentication auth = (Authentication) iUser;
                UserPrincipal up = (UserPrincipal) auth.getPrincipal();

                // fetch từ test id
                var test = mongoDbRepo.findByTestIdWithoutQuestions(testId);
                if (test == null) {
                    throw new SecurityException("Test " + testId + " not found");
                }

                // check var quyền truy cập
                if (!test.getUserId().equals(up.getId())) {
                    throw new SecurityException("Test " + testId + " cannot be access by you");
                }

                // check hết hạn
                if (test.getIsEnded() || LocalDateTime.now().isAfter(test.getEndAt())) {
                    throw new RuntimeException("Test " + testId + " expired");
                }

                // thêm thuộc tính access vào để sử dụng ở bên send
                Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
                if (sessionAttributes != null) {
                    TestData obj = (TestData) sessionAttributes.get(TestVars.TEST_ACCEPT_INFO);
                    if (obj == null) {
                        sessionAttributes.put(TestVars.TEST_ACCEPT_INFO, test);
                        TestSocketSession.addNewTestSessions(test.getTestId(), accessor.getSessionId());
                    }
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid testId in destination");
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                throw new IllegalArgumentException("An error when setup test");
            }
        }
    }
}
