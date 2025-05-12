package com.example.quizcards.dto;

import java.time.LocalDateTime;

public interface SetDownloadProjectionDTO {
    Long getSetId();
    String getTitle();
    String getDescriptionSet();
    int getTotalCard();
}
