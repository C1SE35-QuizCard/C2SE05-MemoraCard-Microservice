package com.example.quizcards.dto;

public interface SortListDTO {
    Long getSetId();
    String getTitle();
    Long getUserId();
    String getDateCreatedOrLastAccessed();
    String getAuthor();
    String getAvatar();
    Long getTotalCard();
}
