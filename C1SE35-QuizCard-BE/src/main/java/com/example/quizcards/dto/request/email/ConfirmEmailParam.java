package com.example.quizcards.dto.request.email;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConfirmEmailParam implements IParam {
    String userName;
    String oldEmail;
    String newEmail;
    String link;
    LocalDateTime sentAt;
}
