package com.example.quizcards.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserInfoResponse {
    @JsonProperty("role")
    List<String> role;

    @JsonProperty("id")
    Long id;

    @JsonProperty("username")
    String username;

    @JsonProperty("avatar")
    String avatar;

    @JsonProperty("firstname")
    String firstName;

    @JsonProperty("lastname")
    String lastName;

    @JsonProperty("email")
    String email;

    @JsonProperty("phonenumber")
    String phoneNumber;

    @JsonProperty("gender")
    boolean gender;

    @JsonProperty("address")
    String address;

    @JsonProperty("dateOfBirth")
    LocalDate dateOfBirth;

    @JsonProperty("haspassword")
    boolean hasPassword;
}
