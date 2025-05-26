package com.microservices.streakservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("streak_details")
public class StreakDetails {
    @Id
    @Column("id")
    private Long id;

    @Column("user_id")
    private Long userId;

    @Column("date_learned")
    private LocalDate dateLearned;

    // nếu cần lưu action sau này, có thể thêm
    // @Column("streak_action")
    // private String action;
}