package com.example.quizcards.dto;

import java.time.LocalDateTime;

public interface ICollectionDTO {
    Long getId();
    Long getFolderId();
    String getFolderTitle();
    Long getSetId();
    String getSetTitle();
    String getDescriptionSet();
    Long getUserId();
    String getUserName();
    String getCreatedAt();
    String getUpdatedAt();
    Long getTotalCard();
}
