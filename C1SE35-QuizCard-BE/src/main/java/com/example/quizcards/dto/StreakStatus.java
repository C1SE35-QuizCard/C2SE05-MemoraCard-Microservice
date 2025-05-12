package com.example.quizcards.dto;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.*;
import lombok.experimental.FieldDefaults;

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
    LocalDate date;
    String dayFull;
    String dayShort;
    String dayFullEn;
    String dayShortEn;
    int dayRank;

    public StreakStatus(LocalDate date) {
        this.date = date;
    }
}
