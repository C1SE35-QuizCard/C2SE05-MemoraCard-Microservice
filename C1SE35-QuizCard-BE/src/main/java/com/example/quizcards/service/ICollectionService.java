package com.example.quizcards.service;

import com.example.quizcards.dto.ICollectionDTO;
import com.example.quizcards.dto.ISetFlashcardDTO;
import com.example.quizcards.dto.request.CollectionCreateRequestDTO;
import com.example.quizcards.dto.request.CollectionRequest;

import java.util.List;

public interface ICollectionService {
    ICollectionDTO getCollectionById(Long id);

    List<ICollectionDTO> getAllCollection();
    List<ISetFlashcardDTO> findAllSetToAddFolder(Long folderId);

    List<ICollectionDTO> getCollectionBySetId(Long setId);

    List<ICollectionDTO> getCollectionByFolderId(Long folderId);

    void addCollection(Long folderId, Long setId);

    void deleteCollection(Long id);

    void updateCollection(CollectionRequest request);

    void addCollection_2(CollectionCreateRequestDTO requestDTO);

    void deleteCollection_2(Long collectionId);
    boolean existsByFolderIdAndSetId(Long folderId,Long setId);
    boolean existsByCollectionId(Long collectionId);
}
