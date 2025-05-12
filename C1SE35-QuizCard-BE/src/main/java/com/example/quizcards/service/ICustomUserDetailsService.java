package com.example.quizcards.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface ICustomUserDetailsService {
    UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException;

    UserDetails loadUserById(Long id);

    UserDetails loadUserByUsernameOnly(String usernameOrEmail) throws UsernameNotFoundException;

    UserDetails loadUserByEmailOnly(String usernameOrEmail) throws UsernameNotFoundException;

    UserDetails loadUserByUserCodeOnly(String usernameOrEmail) throws UsernameNotFoundException;
}
