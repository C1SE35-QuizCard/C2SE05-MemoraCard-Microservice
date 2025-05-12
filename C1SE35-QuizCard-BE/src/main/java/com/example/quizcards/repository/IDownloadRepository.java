package com.example.quizcards.repository;

import com.example.quizcards.dto.FlashCardDownloadDTO;
import com.example.quizcards.dto.SetDownloadProjectionDTO;
import com.example.quizcards.dto.IFlashCardDownloadDTO;
import com.example.quizcards.entities.SetFlashcard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IDownloadRepository extends JpaRepository<SetFlashcard, Long> {

    @Query(value = """
            SELECT sf.set_id AS setId, sf.title AS title, sf.description_set AS descriptionSet, 
            COUNT(f.card_id) AS totalCard
            FROM set_flashcards sf
            LEFT JOIN flashcards f ON f.set_id = sf.set_id
            WHERE sf.set_id = :setId
            GROUP BY sf.set_id, sf.title, sf.description_set
            """, nativeQuery = true)
    SetDownloadProjectionDTO findSetInfoById(@Param("setId") Long setId);

    @Query("SELECT new com.example.quizcards.dto.FlashCardDownloadDTO(f.cardId, f.question, f.answer, f.imageLink) " +
            "FROM Flashcard f WHERE f.set.setId = :setId")
    List<FlashCardDownloadDTO> findFlashcardsBySetId(@Param("setId") Long setId);


}
