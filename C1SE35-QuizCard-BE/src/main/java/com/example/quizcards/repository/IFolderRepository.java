package com.example.quizcards.repository;

import com.example.quizcards.dto.IFolderDTO;
import com.example.quizcards.dto.ISetFlashcardDTO;
import com.example.quizcards.entities.Folder;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IFolderRepository extends JpaRepository<Folder, Long> {
    @Query(value = """
            select f.folder_id, f.title, f.created_at, f.updated_at, s.first_name, s.last_name,
                   (select count(*) from collection c where c.folder_id = f.folder_id) as set_count
            from folders f
            join app_users s on f.user_id = s.user_id
            left join collection c on f.folder_id = c.folder_id
            where f.folder_id = :folder_id
            group by f.folder_id, s.first_name, s.last_name
            """, nativeQuery = true)
    IFolderDTO findFolderById(@Param("folder_id") Long folderId);


    @Query(value = """
            select f.folder_id, f.title, 
                   f.created_at, f.updated_at, 
                   a.first_name, a.last_name, 
                   count(l.set_id) as setCount
            from folders f
            join app_users a on f.user_id = a.user_id
            left join collection l on f.folder_id = l.folder_id
            where f.user_id = :userId
            group by f.folder_id, f.title, f.created_at, f.updated_at, a.first_name, a.last_name
            """, nativeQuery = true)
    List<IFolderDTO> findFoldersByUserId(@Param("userId") Long userId);


    @Query(value = """
            SELECT\s
                f.folder_id,\s
                f.title,\s
                f.created_at,\s
                f.updated_at,\s
                s.first_name,\s
                s.last_name,
                (SELECT COUNT(*) FROM collection c WHERE c.folder_id = f.folder_id) AS set_count
            FROM\s
                folders f
            JOIN\s
                app_users s ON f.user_id = s.user_id
            LEFT JOIN\s
                collection c ON f.folder_id = c.folder_id
            WHERE\s
                (MATCH(f.title) AGAINST(:title IN BOOLEAN MODE) OR f.title LIKE CONCAT('%',:title, '%'))\s
                AND\s
                s.user_id=:user_id
            GROUP BY\s
                f.folder_id, s.first_name, s.last_name;
            """, nativeQuery = true)
    List<IFolderDTO> searchFolderByTitle(@Param("title") String title,@Param("user_id")Long userId);

    @Query(value = """
            SELECT s.set_id, s.title, s.description_set, s.created_at, s.updated_at,
                s.is_approved, s.is_anonymous, s.sharing_mode, a.first_name, a.last_name,
                a.user_name, a.user_id, a.avatar, m.category_name,
                COUNT(c.card_id) AS total_card,
                CASE
                    WHEN s.hash_password IS NOT NULL AND s.hash_password <> '' THEN 1
                    ELSE 0
                END AS has_password
            FROM set_flashcards s
            JOIN app_users a ON s.user_id = a.user_id
            JOIN category_set_flashcards m ON s.category_id = m.category_id
            JOIN collection l ON l.set_id = s.set_id
            JOIN folders f ON f.folder_id = l.folder_id
            LEFT JOIN flashcards c ON c.set_id = s.set_id
            WHERE f.folder_id = :folder_id
            GROUP BY s.set_id, s.title, s.description_set, s.created_at, s.updated_at,
                s.is_approved, s.is_anonymous, s.sharing_mode, a.first_name, a.last_name,
                a.user_name, a.user_id, a.avatar, m.category_name
            """, nativeQuery = true)
    List<ISetFlashcardDTO> findSetByFolderId(@Param("folder_id") Long folderId);

    @Modifying
    @Transactional
    @Query(value = """
            insert into folders(title, created_at, updated_at, user_id)
            values (:title, now(), now(), :user_id)
            """, nativeQuery = true)
    void createFolder(@Param("title") String title,
                      @Param("user_id") Long userId);


    @Modifying
    @Transactional
    @Query(value = """
            delete from folders f
            where f.folder_id = :folder_id
            """, nativeQuery = true)
    void deleteFolderById(@Param("folder_id") Long folderId);

    @Modifying
    @Transactional
    @Query(value = """
            update folders f
            set f.title = :title, f.updated_at = now(), f.user_id = :user_id
            where f.folder_id = :folder_id
            """, nativeQuery = true)
    void updateFolder(@Param("folder_id") Long folderId,
                      @Param("title") String title,
                      @Param("user_id") Long userId);
}
