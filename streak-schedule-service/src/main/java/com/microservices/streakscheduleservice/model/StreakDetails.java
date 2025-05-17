package com.microservices.scheduleservice.model;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table("streak_details")
public class StreakDetails {

    /** AUTO_INCREMENT (IDENTITY) vẫn được driver MySQL-R2DBC hỗ trợ */
    @Id
    Long id;

    /** FK tới bảng app_user */
    @Column("user_id")
    Long userId;

    @Column("date_learned")
    LocalDate dateLearned;

    /*
    // Nếu cần lưu action, dùng STRING hoặc INT + converter:
    @Column("streak_action")
    StreakAction action;
    enum StreakAction { LEARNED, FREEZE }
    */
}
