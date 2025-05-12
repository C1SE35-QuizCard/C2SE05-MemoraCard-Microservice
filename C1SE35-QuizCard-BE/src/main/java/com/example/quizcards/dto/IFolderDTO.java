package com.example.quizcards.dto;

import java.time.LocalDateTime;

public interface IFolderDTO {
    Long getFolderId();

    String getTitle();

    LocalDateTime getCreatedAt();

    LocalDateTime getUpdatedAt();

    String getFirstName();

    String getLastName();

    int getSetCount();
}
