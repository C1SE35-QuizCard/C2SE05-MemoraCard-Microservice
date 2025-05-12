package com.example.quizcards.dto.request;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomRequestDTO {
    private Long setId;
    private String roomName;
    private Integer numQuestions;
    private Integer timePerQuestion;
    private Integer maxJoiners;
}
