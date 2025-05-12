package com.example.quizcards.repository;

import com.example.quizcards.dto.IAppUserDTO;
import com.example.quizcards.entities.AppUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IAppUserRepository extends JpaRepository<AppUser, Long> {
    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUserCode(String userCode);

    Optional<AppUser> findByUsername(String username);

    Optional<AppUser> findByEmail(String email);

    Optional<AppUser> findByUserCode(String userCode);

    Optional<AppUser> findByUsernameOrEmail(String username, String email);


    @Query(value = """
            select a.user_id, 
            a.address, 
            a.avatar, 
            a.date_create, 
            a.date_of_birth, 
            a.email,
            a.enabled as enabled,
            a.first_name,
            a.gender as gender,
            a.last_name,
            a.phone_number,
            a.user_name as username,
            a.role_id,
            ar.role_name
            from app_users a
            join app_roles ar on a.role_id = ar.role_id
            where ar.role_name != 'ROLE_ADMIN'
            """,
            countQuery = """
                                    select count(a.user_id)
                                    from app_users a
                                    join app_roles ar on a.role_id = ar.role_id
                                    where ar.role_name != 'ROLE_ADMIN'
                    """,
            nativeQuery= true)
    Page<IAppUserDTO> getAll(Pageable pageable);

    @Query(value = """
            select a.user_id, 
            a.address, 
            a.avatar, 
            a.date_create, 
            a.date_of_birth, 
            a.email,
            a.enabled as enabled,
            a.first_name,
            a.gender as gender,
            a.last_name,
            a.phone_number,
            a.user_name as username,
            a.role_id,
            ar.role_name
            from app_users a
            join app_roles ar on a.role_id = ar.role_id
            where ar.role_name != 'ROLE_ADMIN'
            """, nativeQuery = true)
    List<IAppUserDTO> getAll();

    @Query(value = """
            select a.user_id, 
            a.address, 
            a.avatar, 
            a.date_create, 
            a.date_of_birth, 
            a.email,
            a.enabled as enabled,
            a.first_name,
            a.gender as gender,
            a.last_name,
            a.phone_number,
            a.user_name as username,
            a.role_id,
            ar.role_name
            from app_users a
            join app_roles ar on a.role_id = ar.role_id
            where a.user_id = :user_id
            """, nativeQuery = true)
    IAppUserDTO detailUser(@Param("user_id") Long userId);

    @Query("SELECT COUNT(u) > 0 FROM AppUser u WHERE u.username = :username AND u.userId != :id")
    boolean existsByUsernameExcludingUserId(@Param("username") String username, @Param("id") Long userId);

    @Query("SELECT COUNT(u) > 0 FROM AppUser u WHERE u.email = :email AND u.userId != :id")
    boolean existsByEmailExcludingUserId(@Param("email") String email, @Param("id") Long userId);
}
