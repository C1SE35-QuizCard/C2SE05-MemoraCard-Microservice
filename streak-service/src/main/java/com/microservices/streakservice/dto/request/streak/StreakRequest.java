package com.microservices.streakservice.dto.request.streak;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.microservices.streakservice.validation.ValidOffset;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StreakRequest {
//    @NotNull
//    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
//    @ValidOffset
//    OffsetDateTime timeFromClient;

    //    @JsonSetter("timeFromClient")
    //    public void setTimeFromClient(String timeStr) {
    //        this.timeFromClient = OffsetDateTime.parse(timeStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    //    }
    //
    //    @JsonGetter("timeFromClient")
    //    public String getTimeFromClientAsString() {
    //        if (timeFromClient != null) {
    //            return timeFromClient.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    //        }
    //        return null; // Không xảy ra vì @NotNull đảm bảo không null
    //    }

    @NotNull
    @JsonFormat(
            shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"
    )
    @JsonDeserialize(using = com.microservices.streakservice.utils.PreserveOffsetDateTimeDeserializerUtils.class)
    @ValidOffset
    OffsetDateTime timeFromClient;
}
