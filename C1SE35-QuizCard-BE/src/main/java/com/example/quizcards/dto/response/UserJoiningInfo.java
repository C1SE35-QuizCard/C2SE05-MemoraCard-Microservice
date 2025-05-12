package com.example.quizcards.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


public interface UserJoiningInfo {
    Long getUserId();
    String getUserName();
    String getAvatar();

}
