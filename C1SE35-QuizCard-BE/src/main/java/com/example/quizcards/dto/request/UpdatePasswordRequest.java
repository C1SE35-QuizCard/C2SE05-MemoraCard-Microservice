package com.example.quizcards.dto.request;

import com.example.quizcards.exception.BadRequestException;
import com.example.quizcards.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePasswordRequest {
    @NotNull(message = "Old password is required")
    private String oldPassword;

    @NotBlank(message = "New password is required")
    @ValidPassword
    private String newPassword;

    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;

    public void validate() {
        if (!getConfirmPassword().equals(getNewPassword())) {
            throw new BadRequestException("Confirm passwords do not match!");
        } else if (getOldPassword().equals(getNewPassword())) {
            throw new BadRequestException("Cannot use old password as new password!");
        }
    }
}
