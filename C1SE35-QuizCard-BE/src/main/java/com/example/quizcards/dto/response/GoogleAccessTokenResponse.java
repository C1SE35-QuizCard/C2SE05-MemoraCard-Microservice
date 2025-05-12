package com.example.quizcards.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleAccessTokenResponse {
    private String access_token;
    private String token_type;
    private Long expires_in;
}
