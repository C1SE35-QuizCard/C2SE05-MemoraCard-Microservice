package com.microservices.identityservice.controller;

import com.microservices.identityservice.dto.request.GroupRequest;
import com.microservices.identityservice.dto.request.RoleRequest;
import com.microservices.identityservice.dto.request.UserLoginRequest;
import com.microservices.identityservice.dto.request.UserRegisterRequest;
import jakarta.validation.Valid;
import jakarta.ws.rs.core.Response;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.keycloak.admin.client.Keycloak;

import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@RestController
@RequestMapping("/api/identity")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {
    Keycloak keycloak;

    RestTemplate restTemplate = new RestTemplate();

    @Value("${keycloak.admin.server-url}")
    @NonFinal
    String serverUrl;

    @Value("${keycloak.admin.realm}")
    @NonFinal
    String realm;

    @Value("${keycloak.admin.admin-realm-client-id}")
    @NonFinal
    String adminRealmClientId;

    @Value("${keycloak.admin.admin-realm-client-secret}")
    @NonFinal
    String adminRealmClientSecret;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody UserLoginRequest userRegisterRequest) {
        String tokenUrl = serverUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        // Tạo body request
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("client_id", adminRealmClientId);
        body.add("client_secret", adminRealmClientSecret); // Thay bằng client secret thực tế
        body.add("username", userRegisterRequest.getUsername());
        body.add("password", userRegisterRequest.getPassword());

        // Tạo headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Gửi request
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(tokenUrl, request, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return ResponseEntity.ok(response.getBody()); // Trả về JSON chứa access_token
        }
        return ResponseEntity.status(response.getStatusCode()).build();
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody UserRegisterRequest userRegisterRequest) {
        UserRepresentation user = getUserRepresentation(userRegisterRequest);

        Response response = keycloak.realm(realm).users().create(user);
        if (response.getStatus() == HttpStatus.CREATED.value()) {
            String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
            return login(UserLoginRequest.builder()
                    .username(userRegisterRequest.getUsername())
                    .password(userRegisterRequest.getPassword())
                    .build()); // Gọi login để lấy token
        }
        return ResponseEntity.status(response.getStatus()).build();
    }

    private static UserRepresentation getUserRepresentation(UserRegisterRequest userRegisterRequest) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(userRegisterRequest.getUsername());
        user.setFirstName(userRegisterRequest.getFirstName());
        user.setLastName(userRegisterRequest.getLastName());
        user.setEmail(userRegisterRequest.getEmail());
        user.setEnabled(true);

        CredentialRepresentation passwordCred = new CredentialRepresentation();
        passwordCred.setTemporary(false);
        passwordCred.setType(CredentialRepresentation.PASSWORD);
        passwordCred.setValue(userRegisterRequest.getPassword());
        user.setCredentials(Collections.singletonList(passwordCred));
        return user;
    }

    @GetMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserRepresentation> getUser(@PathVariable String userId) {
        UserRepresentation user = keycloak.realm(realm).users().get(userId).toRepresentation();
        return ResponseEntity.ok(user);
    }

    @PutMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateUser(@PathVariable String userId, @RequestBody UserRegisterRequest userRegisterRequest) {
        UserRepresentation user = keycloak.realm(realm).users().get(userId).toRepresentation();
        user.setUsername(userRegisterRequest.getUsername());
        if (userRegisterRequest.getPassword() != null) {
            CredentialRepresentation passwordCred = new CredentialRepresentation();
            passwordCred.setTemporary(false);
            passwordCred.setType(CredentialRepresentation.PASSWORD);
            passwordCred.setValue(userRegisterRequest.getPassword());
            user.setCredentials(Collections.singletonList(passwordCred));
        }
        keycloak.realm(realm).users().get(userId).update(user);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
        keycloak.realm(realm).users().get(userId).remove();
        return ResponseEntity.noContent().build();
    }

    // Thêm role cho user
    @PostMapping("/users/{userId}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> addRoleToUser(@PathVariable String userId, @RequestBody RoleRequest roleRequest) {
        RoleRepresentation role = keycloak.realm(realm).roles().get(roleRequest.getRoleName()).toRepresentation();
        keycloak.realm(realm).users().get(userId).roles().realmLevel().add(Collections.singletonList(role));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users/{userId}/groups")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> addGroupToUser(@PathVariable String userId, @RequestBody GroupRequest groupRequest) {
        GroupRepresentation group = keycloak.realm(realm).groups().group(groupRequest.getGroupId()).toRepresentation();
        keycloak.realm(realm).users().get(userId).joinGroup(group.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/test")
    public String test(@AuthenticationPrincipal Jwt jwt) {
        return "Subject: " + jwt.getSubject() + ", Claims: " + jwt.getClaims();
    }
}
