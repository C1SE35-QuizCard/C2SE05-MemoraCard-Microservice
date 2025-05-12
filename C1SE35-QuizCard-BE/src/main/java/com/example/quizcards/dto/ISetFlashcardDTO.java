package com.example.quizcards.dto;

import java.time.LocalDateTime;

public interface ISetFlashcardDTO {
    Long getSetId();

    String getTitle();

    String getDescriptionSet();

    LocalDateTime getCreatedAt();

    LocalDateTime getUpdatedAt();

    Boolean getIsApproved();

    Boolean getIsAnonymous();

    Boolean getSharingMode();

    String getFirstName();

    String getLastName();

    String getUserName();

    Long getUserId();

    String getAvatar();

    String getCategoryName();

    int getTotalCard();

    int getHasPassword();
}
