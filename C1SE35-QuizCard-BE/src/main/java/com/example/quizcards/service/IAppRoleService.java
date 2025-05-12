package com.example.quizcards.service;

import com.example.quizcards.entities.AppRole;

import java.util.Optional;

public interface IAppRoleService {
    Optional<AppRole> findByRoleName(String name);
}
