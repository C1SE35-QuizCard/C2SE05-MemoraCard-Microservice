package com.example.quizcards.repository;

import com.example.quizcards.dto.ICollectionDTO;
import com.example.quizcards.dto.ISetFlashcardDTO;
import com.example.quizcards.entities.Collection;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ICollectionRepository extends JpaRepository<Collection, Long> {
    @Query(value = """
            select c.id, f.folder_id, f.title, s.set_id, s.title, s.description_set,
                     au.user_id, au.user_name, au.first_name, au.last_name, c.created_at, c.updated_at
            from collection c
            join folders f on f.folder_id = c.folder_id
            join set_flashcards s on s.set_id = c.set_id
            join app_users au on au.user_id = f.user_id
            where c.id = :id
            """, nativeQuery = true)
    ICollectionDTO findCollectionById(@Param("id") Long id);

    @Query(value = """
              select c.id, f.folder_id, f.title, s.set_id, s.title, s.description_set,
                     au.user_id, au.user_name, au.first_name, au.last_name, c.created_at, c.updated_at
              from collection c
              join folders f on f.folder_id = c.folder_id
              join set_flashcards s on s.set_id = c.set_id
              join app_users au on au.user_id = f.user_id
            """, nativeQuery = true)
    List<ICollectionDTO> findAllCollection();

    @Query(value = """
            select c.id, f.folder_id, f.title, s.set_id, s.title, s.description_set,
                     au.user_id, au.user_name, au.first_name, au.last_name, c.created_at, c.updated_at
            from collection c
            join folders f on f.folder_id = c.folder_id
            join set_flashcards s on s.set_id = c.set_id
            join app_users au on au.user_id = f.user_id
            where c.set_id = :set_id
            """, nativeQuery = true)
    List<ICollectionDTO> findCollectionBySetId(@Param("set_id") Long setId);

    @Query(value = """
             select c.id, f.folder_id, f.title as folder_title, s.set_id, s.title as set_title, s.description_set,
                                                  au.user_id, au.user_name,c.created_at, c.updated_at,
                                                  count(distinct fl.card_id) as total_card
                                                  from collection c
                                                  join folders f on f.folder_id = c.folder_id
                                                  join flashcards fl on fl.set_id=c.set_id
                                                  join set_flashcards s on s.set_id = c.set_id
                                                  join app_users au on au.user_id =s.user_id
                                                  where c.folder_id =:folder_id
                                                  group by s.set_id
            """, nativeQuery = true)
    List<ICollectionDTO> findCollectionByFolderId(@Param("folder_id") Long folderId);



    @Query(value ="SELECT \n" +
            "    s.set_id, \n" +
            "    s.title, \n" +
            "    s.description_set, \n" +
            "    s.created_at, \n" +
            "    s.updated_at, \n" +
            "    s.is_approved, \n" +
            "    s.is_anonymous, \n" +
            "    s.sharing_mode,\n" +
            "    a.last_name, \n" +
            "    a.first_name, \n" +
            "    a.user_name,\n" +
            "    a.user_id, \n" +
            "    a.avatar, \n" +
            "    c.category_name, \n" +
            "    COUNT(f.card_id) AS total_card\n," +
            "    CASE\n" +
            "       WHEN s.hash_password IS NOT NULL AND s.hash_password <> '' THEN 1\n" +
            "       ELSE 0\n" +
            "    END AS has_password\n" +
            "FROM \n" +
            "    set_flashcards s\n" +
            "JOIN \n" +
            "    app_users a ON s.user_id = a.user_id\n" +
            "JOIN \n" +
            "    category_set_flashcards c ON s.category_id = c.category_id\n" +
            "JOIN \n" +
            "    flashcards f ON f.set_id = s.set_id\n" +
            "LEFT JOIN \n" +
            "    user_flashcard_settings ufs ON s.set_id = ufs.set_id AND ufs.user_id =:user_id\n" +
            "WHERE \n" +
            "    (s.user_id =:user_id OR ufs.user_id =:user_id) \n" +
            "    AND s.set_id NOT IN (\n" +
            "        SELECT \n" +
            "            s_inner.set_id\n" +
            "        FROM \n" +
            "            collection c\n" +
            "        JOIN \n" +
            "            folders f ON f.folder_id = c.folder_id\n" +
            "        JOIN \n" +
            "            flashcards fl ON fl.set_id = c.set_id\n" +
            "        JOIN \n" +
            "            set_flashcards s_inner ON s_inner.set_id = c.set_id\n" +
            "        JOIN \n" +
            "            app_users au ON au.user_id = f.user_id\n" +
            "        WHERE \n" +
            "            c.folder_id =:folder_id\n" +
            "        GROUP BY \n" +
            "            s_inner.set_id\n" +
            "    )\n" +
            "GROUP BY \n" +
            "    s.set_id, \n" +
            "    s.title, \n" +
            "    s.description_set, \n" +
            "    s.created_at, \n" +
            "    s.updated_at, \n" +
            "    s.is_approved, \n" +
            "    s.is_anonymous, \n" +
            "    s.sharing_mode,\n" +
            "    a.last_name, \n" +
            "    a.first_name, \n" +
            "    a.user_name,\n" +
            "    a.user_id, \n" +
            "    a.avatar, \n" +
            "    c.category_name\n" +
            "ORDER BY \n" +
            "    s.created_at DESC;" +
            "", nativeQuery = true)
    List<ISetFlashcardDTO> findAllSetToAddFolder(@Param("user_id") Long userId, @Param("folder_id") Long folderId);
    @Modifying
    @Transactional
    @Query(value = """
            insert into collection(folder_id, set_id, created_at, updated_at)
            values (:folder_id, :set_id, now(), now())
            """, nativeQuery = true)
    void createCollection(@Param("folder_id") Long folderId,
                          @Param("set_id") Long setId);


    @Modifying
    @Transactional
    @Query(value = """
            delete from collection c
            where c.id = :id
            """, nativeQuery = true)
    void deleteCollectionById(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query(value = """
            update collection c
            set c.folder_id = :folder_id, c.set_id = :set_id, c.updated_at = now()
            where c.id = :id
            """, nativeQuery = true)
    void updateCollection(@Param("id") Long id,
                          @Param("folder_id") Long folderId,
                          @Param("set_id") Long setId);


    @Query(value = """
            select count(1)
            from collection c
            where c.folder_id = :folder_id and c.set_id = :set_id
            """, nativeQuery = true)
    Integer countSetsByFolderIdAndSetId(@Param("folder_id") Long folderId,
                                        @Param("set_id") Long setId);

    @Query(value = "SELECT COUNT(1) FROM collection c WHERE c.id =:collection_id", nativeQuery = true)
    Integer existsByCollectionId(@Param("collection_id") Long collectionId);


}
