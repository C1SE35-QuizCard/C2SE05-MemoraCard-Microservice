package com.example.quizcards.helpers.CollectionHelpers;

import com.example.quizcards.dto.request.CollectionCreateRequestDTO;
import com.example.quizcards.dto.request.CollectionParamRequest;
import com.example.quizcards.dto.request.CollectionRequest;

public interface ICollectionHelpers {
    void handleDeleteCollection(Long collectionId);

    void handleAddCollection(CollectionCreateRequestDTO request);

    void handleAdminDeleteCollection(CollectionParamRequest request, Long userId);

    void handleAdminAddCollection(CollectionParamRequest request, Long userId);
}
