package com.example.quizcards.repository;

import com.example.quizcards.dto.ICategorySetFlashcardDTO;
import com.example.quizcards.dto.ISetFlashcardDTO;
import com.example.quizcards.entities.CategorySetFlashcard;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import java.util.List;

@Repository
public interface ICategorySetFlashcardRepository extends JpaRepository<CategorySetFlashcard, Long> {
    @Query(value = """
            select c.category_id, c.category_name
            from category_set_flashcards c
            where c.category_id = :category_id
            """, nativeQuery = true)
    ICategorySetFlashcardDTO findCategorySetFlashcardById(@Param("category_id") Long categoryId);

    List<ICategorySetFlashcardDTO> findByCategoryName(String categoryName);

    @Query(value = """
            select s.set_id, s.title, s.description_set, s.created_at, s.updated_at, s.is_approved,
                   s.is_anonymous, s.sharing_mode, a.first_name, a.last_name, a.user_name, a.user_id,
                   a.avatar, c.category_name, COUNT(f.card_id) as TotalCard,
            CASE
                WHEN s.hash_password IS NOT NULL AND s.hash_password <> '' THEN 1
                ELSE 0
            END AS has_password
            from set_flashcards s
            join app_users a on s.user_id = a.user_id
            join category_set_flashcards c on s.category_id = c.category_id
            join flashcards f on f.set_id = s.set_id
            where s.category_id = :category_id and s.sharing_mode = true
            group by s.set_id, s.title, s.description_set, s.created_at, s.updated_at, s.is_approved,
                   s.is_anonymous, s.sharing_mode, a.first_name, a.last_name, a.user_name, a.user_id,
                   a.avatar, c.category_name
            """, nativeQuery = true)
    List<ISetFlashcardDTO> findAllSetFlashcardsByCategoryId(@Param("category_id") Long categoryId);

    @Query(value = """
            select c.category_id, c.category_name
            from category_set_flashcards c
            """, nativeQuery = true)
    List<ICategorySetFlashcardDTO> findAllCategorySetFlashcard();

    @Query(value = """
            SELECT\s
                c.category_id,\s
                c.category_name,
                COUNT(s.set_id) AS total_sets,  -- Số lượng bộ flashcard trong mỗi danh mục
                MAX(ufs.last_accessed) AS last_accessed  -- Lấy thời gian truy cập gần nhất
            FROM\s
                category_set_flashcards c
            JOIN\s
                set_flashcards s ON s.category_id = c.category_id
            JOIN\s
                user_flashcard_settings ufs ON s.set_id = ufs.set_id
            GROUP BY\s
                c.category_id, c.category_name
            ORDER BY\s
                total_sets DESC, last_accessed DESC  -- Sắp xếp theo số bộ flashcard và thời gian truy cập gần nhất
            LIMIT 10;
            """, nativeQuery = true)
    List<ICategorySetFlashcardDTO> findTopMostAccessedCategory();

    @Modifying
    @Transactional
    @Query(value = """
            insert into category_set_flashcards(category_name)
            values (:category_name)
            """, nativeQuery = true)
    void createCategorySetFlashcard(@Param("category_name") String categoryName);


    @Modifying
    @Transactional
    @Query(value = """
            delete from category_set_flashcards c
            where c.category_id = :category_id
            """, nativeQuery = true)
    void deleteCategorySetFlashcard(@Param("category_id") Long categoryId);

    @Modifying
    @Transactional
    @Query(value = """
            update category_set_flashcards c
            set c.category_name = :category_name
            where c.category_id = :category_id
            """, nativeQuery = true)
    void updateCategorySetFlashcard(@Param("category_id") Long categoryId,
                                    @Param("category_name") String categoryName);

    @Query(value = """
            select s.set_id, s.title, s.description_set, s.created_at, s.updated_at, s.is_approved,
                   s.is_anonymous, s.sharing_mode, a.first_name, a.last_name, a.user_name, a.user_id,
                   a.avatar, c.category_name, COUNT(f.card_id) as TotalCard,
                   CASE
                        WHEN s.hash_password IS NOT NULL AND s.hash_password <> '' THEN 1
                        ELSE 0
                   END AS has_password
            from set_flashcards s
            join app_users a on s.user_id = a.user_id
            join category_set_flashcards c on s.category_id = c.category_id
            join flashcards f on f.set_id = s.set_id
            where s.category_id = :categoryId and s.sharing_mode = true
            group by s.set_id, s.title, s.description_set, s.created_at, s.updated_at, s.is_approved,
                   s.is_anonymous, s.sharing_mode, a.first_name, a.last_name, a.user_name, a.user_id,
                   a.avatar, c.category_name
            """, nativeQuery = true)
    Page<ISetFlashcardDTO> findAllSetFlashcardsByCategoryId2(@Param("categoryId") Long categoryId,
                                                             Pageable pageable);


    @Query(value = """
                select c.category_id, c.category_name
                from category_set_flashcards c
            """,
            countQuery = """
               select count(c.category_id)
                from category_set_flashcards c
            """,
            nativeQuery = true)
    Page<ICategorySetFlashcardDTO> getAllByPagination(Pageable pageable);
}
