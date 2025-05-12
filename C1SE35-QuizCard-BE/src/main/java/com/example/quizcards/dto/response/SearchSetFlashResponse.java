package com.example.quizcards.dto.response;


public interface SearchSetFlashResponse {

    Long getSetId();

    String getTitle();

    String getDescriptionSet();

    Boolean getIsAnonymous();

    String getUserName();

    Long getUserId();

    String getAvatar();

    String getCategoryName();

    Long getTotalCard();

    int getHasPassword();
}

