package com.example.quizcards.service;

import com.example.quizcards.dto.IFolderDTO;
import com.example.quizcards.dto.ISetFlashcardDTO;
import com.example.quizcards.dto.request.FolderRequest;
import com.example.quizcards.dto.request.UpdateFolderRequest;

import java.util.List;

public interface IFolderService {
    IFolderDTO getFolderById(Long folderId);

    List<IFolderDTO> getFoldersByUserId();

    List<IFolderDTO> searchFolderByTitle(String title);

    List<ISetFlashcardDTO> getSetByFolderId(Long folderId);

    List<ISetFlashcardDTO> findSetByFolderIdAndUserId(Long folderId);

    void addFolder(String title);

    void deleteFolder(Long folderId);

    void updateFolder(UpdateFolderRequest request);
    void addFolder_2(FolderRequest request);
    void deleteFolder_2(Long folderId);
    void updateFolder_2(FolderRequest request);
}
