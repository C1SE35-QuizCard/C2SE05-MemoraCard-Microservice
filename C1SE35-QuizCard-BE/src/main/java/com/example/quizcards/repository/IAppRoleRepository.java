package com.example.quizcards.repository;

import com.example.quizcards.entities.AppRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IAppRoleRepository extends JpaRepository<AppRole, Long> {
    Optional<AppRole> findByRoleName(String name);
}
