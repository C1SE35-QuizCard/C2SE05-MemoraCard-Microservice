package com.example.quizcards.repository;

import com.example.quizcards.dto.IExamDetailDTO;
import com.example.quizcards.entities.ExamDetail;
import com.example.quizcards.entities.UserProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IExamDetailRepository extends JpaRepository<ExamDetail, Long> {
    @Query(value = """
            select count(1)
            from exam_details e
            where e.ex_id = :id
            """, nativeQuery = true)
    Integer countExamDetailById(@Param("id") Long examDetailId);

    @Query(value = """
            select * from exam_details e
            where e.test_id = :test_id and e.flashcard_id = :card_id
            """, nativeQuery = true)
    ExamDetail findExamDetailByTestIdAndCardId(@Param("test_id") Long testId,
                                               @Param("card_id") Long cardId);


    @Query(value = """
            select ex_id,f.question, your_answer, f.answer ,is_true, t.test_mode_id
            from exam_details e
            join flashcards f on f.card_id = e.flashcard_id
            join test t on t.test_id = e.test_id
            where e.test_id = :test_id
            """, nativeQuery = true)
    List<IExamDetailDTO> findExamDetailsByTestId(@Param("test_id") Long testId);
}