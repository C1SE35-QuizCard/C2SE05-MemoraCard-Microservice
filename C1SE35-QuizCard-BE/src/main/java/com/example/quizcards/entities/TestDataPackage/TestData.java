package com.example.quizcards.entities.TestDataPackage;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "test_exams")
@CompoundIndex(name = "testId_userId_idx", def = "{'testId': 1, 'userId': 1}") // Index gộp
@CompoundIndex(name = "userId_setId_idx", def = "{'userId': 1, 'setId': 1}") // Index gộp
public class TestData {
    @Id
    @JsonIgnore
    private String id; // MongoDB ObjectId

    @Indexed(unique = true)
    private Long testId;

    @Indexed
    private Long userId;

    @Indexed
    private Long setId;

    private Long testModeId;

    private String testModeName;

    private LocalDateTime createdAt;

    @Indexed
    private LocalDateTime endAt;

    private Boolean isEnded;

    private Long totalQuestions;

    private Long goalScore;

    private Long numQuestionsTrue;

    @LastModifiedDate // Tự động gán thời gian khi bản ghi bị chỉnh sửa
    @JsonIgnore
    private LocalDateTime updatedAt;

    private List<IQuestion> questions;
}
