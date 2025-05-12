package com.example.quizcards.entities;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class UserRankingId implements Serializable {
    private Long roomId;
    private Long questionId;
    private Long userId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserRankingId that = (UserRankingId) o;
        return Objects.equals(roomId, that.roomId) &&
                Objects.equals(questionId, that.questionId) &&
                Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roomId, questionId, userId);
    }
}
