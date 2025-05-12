package com.example.quizcards.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoogleInfoUser {
    private String userName;

    private String email;

    private String firstName;

    private String lastName;

    private String avatarUrl;

    private String userCode;

    private String sub;

    private Boolean enabled;
}
