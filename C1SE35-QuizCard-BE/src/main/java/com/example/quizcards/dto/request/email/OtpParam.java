package com.example.quizcards.dto.request.email;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OtpParam implements IParam {
    String yourOtpCode;
    String emailAddress;
    String username;
    LocalDateTime sentAt;
}
