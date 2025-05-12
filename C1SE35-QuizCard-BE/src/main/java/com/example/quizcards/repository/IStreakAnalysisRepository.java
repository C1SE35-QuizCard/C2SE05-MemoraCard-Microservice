package com.example.quizcards.repository;

import com.example.quizcards.entities.AppUser;
import com.example.quizcards.entities.StreakAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IStreakAnalysisRepository extends JpaRepository<StreakAnalysis, Long> {
    Optional<StreakAnalysis> findByUser(AppUser user);
}
