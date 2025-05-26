package com.microservices.streakservice.dto;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.relational.core.mapping.Column;

import java.time.LocalDate;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class StreakStatus {
    enum Status {
        LEARNED("Learned"),
        FREEZE("Freeze");

        private final String displayName;

        Status(String displayName) {
            this.displayName = displayName;
        }

        @JsonValue
        public String getDisplayName() {
            return displayName;
        }
    }

    // Status status;
    @Column("date_learned")
    LocalDate date;

    @Column("date_full")
    String dayFull;

    @Column("date_short")
    String dayShort;

    @Column("date_full_en")
    String dayFullEn;

    @Column("date_short_en")
    String dayShortEn;

    @Column("day_rank")
    int dayRank;

    public StreakStatus(LocalDate date) {
        this.date = date;
    }
}
