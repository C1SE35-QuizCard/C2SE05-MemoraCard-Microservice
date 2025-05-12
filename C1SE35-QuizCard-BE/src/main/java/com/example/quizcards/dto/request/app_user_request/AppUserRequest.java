package com.example.quizcards.dto.request.app_user_request;

import com.example.quizcards.entities.AppRole;
import com.example.quizcards.validation.ValidPassword;
import jakarta.persistence.Column;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppUserRequest {
    private String address;

    private String avatar;

    @NotNull(message = "Birthdate is required.")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    private Boolean enabled;

    @NotBlank(message = "Username is required")
    @Pattern(regexp = "^(?!.*--)[a-zA-Z0-9]+(-[a-zA-Z0-9]+)*$",
            message = "Username must only contain letters, numbers, and single hyphens, and no consecutive hyphens")
    @Size(min = 8, max = 50, message = "Username must be between 8 and 50 characters")
    private String username;

    private Boolean gender;

    private String password;

    @Size(max = 255, message = "Phone number must not exceed 255 characters")
//    @Pattern(regexp = "^\\+?[0-9. ()-]{7,25}$", message = "Invalid phone number")
    private String phoneNumber;

    @NotBlank(message = "First name is required.")
    @Pattern(regexp = "^\\S(.*\\S)?$", message = "First name cannot be only whitespace")
    @Size(max = 50, message = "First name must not exceed 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required.")
    @Pattern(regexp = "^\\S(.*\\S)?$", message = "Last name cannot be only whitespace")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    private String lastName;

    @NotNull(message = "Role ID is required.")
    private Long roleId;
}
