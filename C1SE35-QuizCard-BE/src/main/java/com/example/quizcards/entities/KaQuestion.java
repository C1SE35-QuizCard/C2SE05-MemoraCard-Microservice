//package com.example.quizcards.entities;
//
//import jakarta.persistence.*;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//@Data
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//@Entity
//@Table(name = "ka_questions")
//public class KaQuestion {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "ka_question_id")
//    private Long kaQuestionId;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "room_id", nullable = false)
//    private KaRoom kaRoom;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "card_id", nullable = false)
//    private Flashcard flashcard;
//}
package com.example.quizcards.entities;

import com.example.quizcards.utils.AnswerListConverter;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ka_questions")
public class KaQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(name = "answer_list", nullable = false, columnDefinition = "JSON")
    @Convert(converter = AnswerListConverter.class)
    private List<Answer> answerList;

    @Column(name = "answer_id")
    private Long answerId;

    @Column(name = "card_id")
    private Long cardId;

    @Column(name = "room_id", nullable = false)
    private Long roomId; // Liên kết với Room

    @Column(name = "created_at", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private java.util.Date createdAt = new java.util.Date();

    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private java.util.Date updatedAt = new java.util.Date();

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = new java.util.Date();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Answer {
        private Long id;
        private String answer;
        @JsonProperty("isTrue")
        private boolean isTrue;
    }
}
