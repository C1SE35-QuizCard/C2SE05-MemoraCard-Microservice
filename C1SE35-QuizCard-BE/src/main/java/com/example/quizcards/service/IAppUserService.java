package com.example.quizcards.service;

import com.example.quizcards.dto.IAppUserDTO;
import com.example.quizcards.dto.request.app_user_request.*;
import com.example.quizcards.dto.request.email.ConfirmEmailParam;
import com.example.quizcards.dto.request.email.OtpParam;
import com.example.quizcards.entities.AppUser;
import com.example.quizcards.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.Optional;

public interface IAppUserService {
    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUserCode(String userCode);

    Optional<AppUser> findById(Long id);

    Optional<AppUser> findByUsername(String username);

    Optional<AppUser> findByEmail(String email);

    Optional<AppUser> findByUserCode(String userCode);

    Optional<AppUser> findByUsernameOrEmail(String username, String email);

    void save(AppUser user);

    Page<IAppUserDTO> getAllUsersWithPagination(int pages, int size);

//    CompletableFuture<List<IAppUserDTO>> getAllUsers();

    IAppUserDTO detailUser(Long userId);

    IAppUserDTO createAppUser(AppUserRequest request);

    IAppUserDTO updateAppUser(Long userId, AppUserRequest request);

    void deleteAppUser(Long userId);

    void changeBasicInformation(BasicAppUserInformationRequest request);

    boolean checkConfirmedForChangeCriticalInformation(Long userId, HttpServletRequest request);

    OtpParam getOtpForChangeCriticalInformation(Long userId, HttpServletRequest request);

    void createNewOtpForChangeCriticalInformation(UserPrincipal up, HttpServletRequest request);

    void confirmModifyCriticalInformation(ConfirmChangeCriticalInformationRequest confirmRequest, HttpServletRequest request);

    void changeEmail(EmailAppUserRequest emailRequest, HttpServletRequest request);

    void changePassword(PasswordAppUserRequest passwordRequest, HttpServletRequest request);

    void confirmKeyToChangeEmail(String key);

    ConfirmEmailParam getEmailConfirmCriticalInformation();
}
