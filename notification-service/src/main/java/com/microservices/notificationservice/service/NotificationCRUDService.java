package com.microservices.notificationservice.service;

import com.microservices.notificationservice.model.Notification;
import com.microservices.notificationservice.model.NotificationEntry;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationCRUDService {
    ReactiveMongoTemplate mongo;
    private static final String COLL = "notifications";

    public Mono<NotificationEntry> create(String userId, NotificationEntry entry) {
        entry.setNotificationId(UUID.randomUUID().toString());
        entry.setCreatedAt(Instant.now());
        entry.setIsRead(false);

        return mongo.exists(Query.query(Criteria.where("_id").is(userId)), Notification.class)
                .flatMap(exists -> {
                    if (exists) {
                        // Update nếu đã tồn tại
                        Query query = Query.query(Criteria.where("_id").is(userId));
                        Update update = new Update().set("notifications." + entry.getNotificationId(), entry);
                        return mongo.updateFirst(query, update, Notification.class)
                                .thenReturn(entry);
                    } else {
                        // Insert nếu chưa tồn tại
                        Map<String, NotificationEntry> notifications = new HashMap<>();
                        notifications.put(entry.getNotificationId(), entry);
                        Notification notification = Notification.builder()
                                .userId(userId)
                                .notifications(notifications)
                                .build();
                        return mongo.insert(notification)
                                .thenReturn(entry);
                    }
                });
    }

    public Flux<NotificationEntry> getByUser(String userId, int page, int size, boolean preferUnread) {
        int skip = page * size;

        List<AggregationOperation> ops = new ArrayList<>();
        ops.add(Aggregation.match(Criteria.where("_id").is(userId)));
        ops.add(Aggregation.project()
                .and(ObjectOperators.valueOf("notifications").toArray())
                .as("notificationMap"));
        // tách mảng entries thành từng doc
        ops.add(Aggregation.unwind("notificationMap", true));
        ops.add(Aggregation.project().and("notificationMap.v").as("notification"));
        if (preferUnread) {
            // isRead=false (0) trước, isRead=true (1) sau; trong mỗi nhóm sort createdAt desc
            ops.add(Aggregation.sort(
                    Sort.by(
                            Sort.Order.asc("notification.isRead"),
                            Sort.Order.desc("notification.createdAt")
                    )
            ));
        } else {
            // chỉ sort createdAt desc
            ops.add(Aggregation.sort(Sort.Direction.DESC, "notification.createdAt"));
        }
        ops.add(Aggregation.skip(skip));
        ops.add(Aggregation.limit(size));
        ops.add(Aggregation.project().and("notification").as("root"));

        Aggregation aggregation = Aggregation.newAggregation(ops);

        return mongo.aggregate(aggregation, "notifications", Map.class)
                .flatMap(doc -> {
                    Object raw = doc.get("root");
                    NotificationEntry entry;
                    if (raw instanceof NotificationEntry) {
                        entry = (NotificationEntry) raw;
                    } else if (raw instanceof Map<?,?>) {
                        // dùng flatMap thay vì map
                        @SuppressWarnings("unchecked")
                        Map<String, Object> root = (Map<String, Object>) doc.get("root");
                        if (root == null) return Mono.empty();   // ← bỏ qua phần tử rỗng

                        Object rawCreated = root.get("createdAt");
                        Instant createdAt;
                        if (rawCreated instanceof Date) {
                            createdAt = ((Date) rawCreated).toInstant();
                        } else if (rawCreated instanceof Instant) {
                            createdAt = (Instant) rawCreated;
                        } else if (rawCreated instanceof String) {
                            createdAt = Instant.parse((String) rawCreated);
                        } else {
                            createdAt = Instant.EPOCH;
                        }
                        @SuppressWarnings("unchecked")
                        Map<String, Object> payload = (Map<String, Object>) root.get("payload");// hoặc Mono.empty() tuỳ ý

                        entry = NotificationEntry.builder()
                                .notificationId((String) root.get("notificationId"))
                                .createdAt(createdAt)
                                .message((String) root.get("message"))
                                .urlLink((String) root.get("urlLink"))
                                .payload(payload)
                                .isRead((Boolean) root.getOrDefault("isRead", Boolean.FALSE))
                                .build();
                    } else {
                        return Mono.empty();
                    }

                    return Mono.just(entry);
                });
    }

//    public Flux<NotificationEntry> getByIds(String userId, List<String> ids) {
//        if (ids == null || ids.isEmpty()) {
//            return Flux.empty();
//        }
//
//        Aggregation agg = Aggregation.newAggregation(
//                Aggregation.match(Criteria.where("_id").is(userId)),
//
//                // giữ lại phần tử notifications có notificationId ∈ ids
//                Aggregation.project()
//                        .and(ArrayOperators.Filter.filter("notifications")
//                                .as("entry")
//                                .by((AggregationExpression) Criteria.where("entry.notificationId").in(ids)))
//                        .as("filtered"),
//
//                Aggregation.unwind("filtered", true),
//
//                Aggregation.project()
//                        .and("filtered").as("notification")   // đưa ra field gốc
//        );
//
//
//        return mongo.aggregate(agg, "notifications", Map.class)
//                .flatMap(doc -> {
//                    Object raw = doc.get("notification");
//                    NotificationEntry entry;
//                    if (raw instanceof NotificationEntry) {
//                        entry = (NotificationEntry) raw;
//                    } else if (raw instanceof Map<?,?>) {
//                        @SuppressWarnings("unchecked")
//                        Map<String, Object> notif = (Map<String, Object>) doc.get("notification");
//                        if (notif == null) return Mono.empty();
//
//                        Object rawCreated = notif.get("createdAt");
//                        Instant created;
//                        if (rawCreated instanceof Date) {
//                            created = ((Date) rawCreated).toInstant();
//                        } else if (rawCreated instanceof Instant) {
//                            created = (Instant) rawCreated;
//                        } else if (rawCreated instanceof String) {
//                            created = Instant.parse((String) rawCreated);
//                        } else {
//                            created = Instant.EPOCH;
//                        }
//
//                        @SuppressWarnings("unchecked")
//                        Map<String, Object> payload = (Map<String, Object>) notif.get("payload");
//
//                        entry = NotificationEntry.builder()
//                                .notificationId((String) notif.get("notificationId"))
//                                .createdAt(created)
//                                .message((String) notif.get("message"))
//                                .urlLink((String) notif.get("urlLink"))
//                                .payload(payload)
//                                .isRead(Boolean.TRUE.equals(notif.get("isRead")))
//                                .build();
//                    } else {
//                        return Mono.empty();
//                    }
//
//                    return Mono.just(entry);
//                });
//    }

    public Flux<NotificationEntry> getByIds(String userId, List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return Flux.empty();
        }

        Aggregation agg = Aggregation.newAggregation(
                // 1) Chỉ lấy document của chính user này
                Aggregation.match(Criteria.where("_id").is(userId)),

                // 2) Project: biến field "notifications" thành mảng entries [ { k, v }, ... ]
                Aggregation.project()
                        .and(ObjectOperators.valueOf("notifications").toArray())
                        .as("entries"),

                // 3) Unwind mảng entries
                Aggregation.unwind("entries"),

                // 4) Chỉ giữ những entry.k nằm trong ids
                Aggregation.match(Criteria.where("entries.k").in(ids)),

                // 5) Replace root: trả về thẳng entries.v làm document
                Aggregation.replaceRoot("entries.v")
        );

        return mongo.aggregate(agg, "notifications", NotificationEntry.class);
    }

    public Flux<NotificationEntry> markAsRead(String userId, List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return Flux.empty();
        }

        // Sử dụng Bulk Update để cập nhật nhiều document cùng lúc
        Query query = Query.query(Criteria.where("_id").is(userId));
        Update update = new Update();

        // Thêm tất cả các trường cần update vào một lần update
        for (String id : ids) {
            update.set("notifications." + id + ".isRead", true);
        }

        // Thực hiện update một lần và trả về đối tượng đã được cập nhật
        return mongo.updateFirst(query, update, Notification.class)
                .thenMany(getByIds(userId, ids));
    }
}
