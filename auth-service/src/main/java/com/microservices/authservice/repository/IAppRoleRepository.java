package com.microservices.authservice.repository;

import com.microservices.authservice.entities.AppRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IAppRoleRepository extends JpaRepository<AppRole, Long> {
    Optional<AppRole> findByRoleName(String name);
}
