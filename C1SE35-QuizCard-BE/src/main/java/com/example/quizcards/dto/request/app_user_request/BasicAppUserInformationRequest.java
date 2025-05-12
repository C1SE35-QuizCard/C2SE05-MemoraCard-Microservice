package com.example.quizcards.dto.request.app_user_request;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BasicAppUserInformationRequest {
    String address;

    String avatar;

    @Past(message = "Date of birth must be in the past")
    LocalDate dateOfBirth;

    Boolean gender;

    @Size(max = 255, message = "Phone number must not exceed 255 characters")
//    @Pattern(regexp = "^\\+?[0-9. ()-]{7,25}$", message = "Invalid phone number")
    String phoneNumber;

    @Pattern(regexp = "^\\S(.*\\S)?$", message = "First name cannot be only whitespace")
    @Size(max = 50, message = "First name must not exceed 50 characters")
    String firstName;

    @Pattern(regexp = "^\\S(.*\\S)?$", message = "Last name cannot be only whitespace")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    String lastName;
}
