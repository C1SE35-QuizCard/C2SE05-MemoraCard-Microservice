package com.example.quizcards.service.impl;


import com.example.quizcards.entities.AppRole;
import com.example.quizcards.repository.IAppRoleRepository;
import com.example.quizcards.service.IAppRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AppRoleServiceImpl implements IAppRoleService {
    @Autowired
    private IAppRoleRepository appRoleRepository;

    @Override
    public Optional<AppRole> findByRoleName(String roleName) {
        return appRoleRepository.findByRoleName(roleName);
    }
}
