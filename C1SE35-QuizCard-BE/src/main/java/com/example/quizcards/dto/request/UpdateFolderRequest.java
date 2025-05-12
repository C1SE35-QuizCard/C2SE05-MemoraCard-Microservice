package com.example.quizcards.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFolderRequest {
    private Long folderId;

    @NotBlank(message = "Title of the folder is empty.")
    private String title;
}
