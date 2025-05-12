package com.example.quizcards.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FreeUserProfileResponse {
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private String avatar;
}
