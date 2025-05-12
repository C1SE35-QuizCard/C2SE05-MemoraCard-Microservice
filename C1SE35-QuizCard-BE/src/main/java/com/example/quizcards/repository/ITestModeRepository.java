package com.example.quizcards.repository;

import com.example.quizcards.dto.ITestModeDTO;
import com.example.quizcards.entities.TestMode;
import io.micrometer.common.lang.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ITestModeRepository extends JpaRepository<TestMode, Long> {
    @Query(value = """
            select t.test_mode_id, t.test_mode_name
            from test_mode t
            """, nativeQuery = true)
    List<ITestModeDTO> findAllTestMode();

    @Query(value = """
        select count(test_mode_id) from test_mode where test_mode_id = :test_mode_id
        """, nativeQuery = true)
    Integer exists(@Param("test_mode_id") @NonNull Long testModeId);

    @Query(value = """
        select t.test_mode_id, t.test_mode_name
        from test_mode t
        where t.test_mode_name = :test_mode_name
        """, nativeQuery = true)
    List<ITestModeDTO> findByTestModeName(@Param("test_mode_name") @NonNull String testModeName);

    @Query(value = """
        select t.test_mode_id, t.test_mode_name
        from test_mode t
        where t.test_mode_id = :test_mode_id
        """, nativeQuery = true)
    ITestModeDTO findByTestModeId(@Param("test_mode_id") @NonNull Long testModeId);
}
