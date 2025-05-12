package com.example.quizcards.dto.request.app_user_request;

import com.example.quizcards.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PasswordAppUserRequest {
    String oldPassword;

    @NotBlank(message = "New password is required")
    @ValidPassword
    String newPassword;
}
