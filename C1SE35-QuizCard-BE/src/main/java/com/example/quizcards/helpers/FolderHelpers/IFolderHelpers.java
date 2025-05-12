package com.example.quizcards.helpers.FolderHelpers;

import com.example.quizcards.dto.request.FolderRequest;

public interface IFolderHelpers {

    void handleFolderOwner(Long folderId);

    void handleAdminDeleteFolder(Long folderId, Long userId);

    void handleAdminUpdateFolder(FolderRequest request, Long userId);
}
