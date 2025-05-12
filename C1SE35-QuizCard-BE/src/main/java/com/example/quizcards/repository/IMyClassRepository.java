package com.example.quizcards.repository;

import com.example.quizcards.entities.MyClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IMyClassRepository extends JpaRepository<MyClass, Long> {
    List<MyClass> findByOwners_UserId(Long userId);

    // Tìm các lớp mà user là thành viên (không phải chủ sở hữu)
    @Query("SELECT mc FROM MyClass mc JOIN mc.members m WHERE m.userId = :userId AND mc.owners.userId != :userId")
    List<MyClass> findJoinedClassesByUserId(@Param("userId") Long userId);

    Optional<MyClass> findByClassCode(String classCode);

    @Query("SELECT mc FROM MyClass mc WHERE mc.owners.userId = :userId AND mc.title LIKE %:title%")
    List<MyClass> findByOwners_UserIdAndNameContainingIgnoreCase(@Param("userId") Long userId, @Param("title") String title);

    @Query("SELECT mc FROM MyClass mc JOIN mc.members m WHERE m.userId = :userId AND mc.owners.userId != :userId AND mc.title LIKE %:title%")
    List<MyClass> findJoinedClassesByUserIdAndNameContainingIgnoreCase(@Param("userId") Long userId, @Param("title") String title);

    @Query("SELECT mc FROM MyClass mc WHERE mc.owners.userId = :userId AND mc.classCode LIKE %:classCode%")
    List<MyClass> findByOwners_UserIdAndClassCodeContainingIgnoreCase(@Param("userId") Long userId, @Param("classCode") String classCode);

    @Query("SELECT mc FROM MyClass mc JOIN mc.members m WHERE m.userId = :userId AND mc.owners.userId != :userId AND mc.classCode LIKE %:classCode%")
    List<MyClass> findJoinedClassesByUserIdAndClassCodeContainingIgnoreCase(@Param("userId") Long userId, @Param("classCode") String classCode);

    @Query("SELECT mc FROM MyClass mc WHERE mc.owners.userId = :userId AND (mc.title LIKE %:query% OR mc.classCode LIKE %:query%)")
    List<MyClass> findByOwners_UserIdAndNameContainingIgnoreCaseOrClassCodeContainingIgnoreCase(@Param("userId") Long userId, @Param("query") String query, @Param("query") String query2);

    @Query("SELECT mc FROM MyClass mc JOIN mc.members m WHERE m.userId = :userId AND mc.owners.userId != :userId AND (mc.title LIKE %:query% OR mc.classCode LIKE %:query%)")
    List<MyClass> findJoinedClassesByUserIdAndNameContainingIgnoreCaseOrClassCodeContainingIgnoreCase(@Param("userId") Long userId, @Param("query") String query, @Param("query") String query2);
}