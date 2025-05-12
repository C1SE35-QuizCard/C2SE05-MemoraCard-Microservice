package com.microservices.securityservice.repository;

import com.microservices.securityservice.model.AppRole;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface AppRoleRepository extends R2dbcRepository<AppRole, Long> {
    //    @Query("""
//        SELECT r.role_id, r.role_name
//        FROM app_roles r
//        JOIN app_users ur ON r.role_id = ur.role_id
//        WHERE ur.user_id = :userId
//    """)
//    NO CODE: Flux<AppRole> findRolesByUserId(@Param("userId") Long userId);
//
//    @Query("""
//        SELECT r.role_id, r.role_name
//        FROM app_roles r
//        JOIN app_users ur ON r.role_id = ur.role_id
//        WHERE ur.username = :username
//    """)
//    NO CODE: Flux<AppRole> findRolesByUsername(@Param("username") String username);
    Mono<AppRole> findByRoleId(Long roleId);

    Mono<AppRole> findByRoleName(String roleName);
}
