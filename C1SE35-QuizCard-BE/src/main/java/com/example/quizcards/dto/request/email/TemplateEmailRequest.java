package com.example.quizcards.dto.request.email;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TemplateEmailRequest {
    @NotNull
    Sender sender;

    String subject;

    @NotNull
    Integer templateId;

    IParam params;

    List<MessageVersion> messageVersions;
}
