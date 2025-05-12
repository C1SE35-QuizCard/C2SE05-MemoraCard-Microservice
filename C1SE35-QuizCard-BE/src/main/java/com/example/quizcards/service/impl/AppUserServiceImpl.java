package com.example.quizcards.service.impl;

import com.example.quizcards.dto.IAppUserDTO;
import com.example.quizcards.dto.request.app_user_request.*;
import com.example.quizcards.dto.request.email.*;
import com.example.quizcards.entities.AppRole;
import com.example.quizcards.entities.AppUser;
import com.example.quizcards.entities.role.RoleName;
import com.example.quizcards.exception.AccessDeniedException;
import com.example.quizcards.exception.BadRequestException;
import com.example.quizcards.exception.ResourceConflictException;
import com.example.quizcards.exception.ResourceNotFoundException;
import com.example.quizcards.mapper.AppUserMapper;
import com.example.quizcards.repository.IAppRoleRepository;
import com.example.quizcards.repository.IAppUserRepository;
import com.example.quizcards.security.JwtTokenProvider;
import com.example.quizcards.security.UserPrincipal;
import com.example.quizcards.service.IAppUserService;
import com.example.quizcards.utils.CodeRandom;
import com.example.quizcards.utils.CookieUtils;
import com.example.quizcards.utils.RedisUtils;
import com.example.quizcards.validation.PasswordConstraintValidator;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.MessageFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AppUserServiceImpl implements IAppUserService {
    int MAX_SIZE_PER_PAGE = 20;

    KafkaTemplate<String, Object> kafkaTemplate;

    RedisUtils redisUtils;

    @Value("OTP_CHANGE_PASSWORD")
    @NonFinal
    String otpStringPrefix;

    @Value("IS_CONFIRMED")
    @NonFinal
    String isConfirmedSuffix;

    @Value("IS_SENDED_NEW_EMAIL")
    @NonFinal
    String isSendedEmailSuffix;

    @Value("TOKEN_EMAIL_CONFIRM")
    @NonFinal
    String tokenEmailConfirmPrefix;

    @Value("IS_CHANGED_PASSWORD")
    @NonFinal
    String isChangedPasswordSuffix;

    @Value("OTP_CONFIRM_CRITICAL_INFORMATION")
    @NonFinal
    String otpCriticalInformationPrefix;

    @Value("${app.max-otp-attemp-mins}")
    @NonFinal
    Integer maxOtpAttempt;

    @Value("${app.brevo.template.id-otp}")
    @NonFinal
    Integer idTemplateOtp;

    @Value("${app.brevo.template.id-confirm-link}")
    @NonFinal
    Integer idConfirmLink;

    @Value("${app.critical-information.confirm-duration-hours}")
    @NonFinal
    Integer confirmDurationHours;

    @Value("${app.critical-information.changed-email-after-hours}")
    @NonFinal
    Integer changedEmailAfterHours;

    @Value("${app.link-frontend-client-confirm}")
    @NonFinal
    String frontEndUrl;

    @Value("${app.code-security-confirm}")
    @NonFinal
    String codeEncrypt;

    @Value("${app.code-expire-in-minutes}")
    @NonFinal
    Integer codeExpireInMinutes;

    @Value("${app.critical-information.resend-email-after-secs}")
    @NonFinal
    Integer resendEmailAfterSecs;

    IAppUserRepository userRepository;

    PasswordEncoder passwordEncoder;

    IAppRoleRepository roleRepository;

    AppUserMapper appUserMapper;

    JwtTokenProvider tokenProvider;

    CookieUtils cookieUtils;

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByUserCode(String userCode) {
        return userRepository.existsByUserCode(userCode);
    }

    @Override
    public Optional<AppUser> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<AppUser> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public Optional<AppUser> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public Optional<AppUser> findByUserCode(String userCode) {
        return userRepository.findByUserCode(userCode);
    }

    @Override
    public Optional<AppUser> findByUsernameOrEmail(String username, String email) {
        return userRepository.findByUsernameOrEmail(username, email);
    }

    @Override
    @Transactional
    public void save(AppUser user) {
        userRepository.save(user);
    }

    public AppUser updateUserRole(Long userId, Long roleId) {

        Optional<AppUser> appUserOpt = userRepository.findById(userId);
        if (appUserOpt.isPresent()) {
            AppUser appUser = appUserOpt.get();

            Optional<AppRole> appRoleOpt = roleRepository.findById(roleId);
            if (appRoleOpt.isPresent()) {
                AppRole appRole = appRoleOpt.get();
                appUser.setRole(appRole);

                return userRepository.save(appUser);
            } else {
                throw new RuntimeException("Role not found with id: " + roleId);
            }
        } else {
            throw new RuntimeException("User not found with id: " + userId);
        }
    }

    @Override
    public Page<IAppUserDTO> getAllUsersWithPagination(int pages, int size) {
        if (pages < 0) {
            throw new BadRequestException("Page must be greater than or equal to 0");
        }
        if (size < 1 || size > 100) {
            throw new BadRequestException("Size must be greater than 0 and less than or equal to 100");
        }
        Pageable pageable = PageRequest.of(pages, size);
        return userRepository.getAll(pageable);
    }

    @Override
    public IAppUserDTO detailUser(Long userId) {
        return userRepository.detailUser(userId);
    }


//    @Override
//    @Transactional
//    public IAppUserDTO createAppUser(AppUserRequest request) {
//        AppRole role = roleRepository.findById(request.getRoleId())
//                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", request.getRoleId()));
//        if (existsByUsername(request.getUsername()) || existsByEmail(request.getEmail())) {
//            throw new RuntimeException("Username or email address already in use");
//        }
//        PasswordConstraintValidator validator = new PasswordConstraintValidator();
//        if (!validator.isValid(request.getPassword(), null)) {
//            throw new BadRequestException("Invalid password");
//        }
//        AppUser user = AppUser.builder()
//                .address(request.getAddress())
//                .avatar(request.getAvatar())
//                .dateOfBirth(request.getDateOfBirth())
//                .email(request.getEmail())
//                .hashPassword(passwordEncoder.encode(request.getPassword()))
//                .enabled(request.getEnabled())
//                .username(request.getUsername())
//                .gender(request.getGender() != null && request.getGender())
//                .phoneNumber(request.getPhoneNumber())
//                .firstName(request.getFirstName())
//                .lastName(request.getLastName())
//                .userCode(CodeRandom.generateRandomCode(28))
//                .role(role)
//                .build();
//        return IAppUserDTO.AppUserDTO.from(userRepository.save(user));
//    }
//
//    @Override
//    @Transactional
//    public IAppUserDTO updateAppUser(Long userId, AppUserRequest request) {
//        AppUser appUser = userRepository.findById(userId)
//                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        UserPrincipal up = (UserPrincipal) authentication.getPrincipal();
//        if (!up.getId().equals(appUser.getUserId()) &&
//                appUser.getRole().getRoleName().equals(RoleName.ROLE_ADMIN.name())) {
//            throw new AccessDeniedException("Cannot edit user with role admin");
//        }
//        AppRole role = roleRepository.findById(request.getRoleId())
//                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", request.getRoleId()));
//        if (!appUser.getUsername().equals(request.getUsername()) && existsByUsername(request.getUsername())) {
//            throw new BadRequestException("Username or email address already in use");
//        }
//        if (!appUser.getEmail().equals(request.getEmail()) && existsByEmail(request.getEmail())) {
//            throw new BadRequestException("Email address already in use");
//        }
//        PasswordConstraintValidator validator = new PasswordConstraintValidator();
//        if (request.getPassword() != null && !validator.isValid(request.getPassword(), null)) {
//            throw new BadRequestException("Invalid password");
//        }
//        appUser.setAddress(request.getAddress() == null ? appUser.getAddress() : request.getAddress());
//        appUser.setAvatar(request.getAvatar() == null ? appUser.getAvatar() : request.getAvatar());
//        appUser.setDateOfBirth(request.getDateOfBirth() == null ? appUser.getDateOfBirth() : request.getDateOfBirth());
//        appUser.setEmail(request.getEmail() == null ? appUser.getEmail() : request.getEmail());
//        appUser.setHashPassword(request.getPassword() == null ? appUser.getHashPassword() :
//                passwordEncoder.encode(request.getPassword()));
//        appUser.setEnabled(request.getEnabled() == null ? appUser.getEnabled() : request.getEnabled());
//        appUser.setUsername(request.getUsername() == null ? appUser.getUsername() : request.getUsername());
//        appUser.setGender(request.getGender() == null ? appUser.getGender() : request.getGender());
//        appUser.setPhoneNumber(request.getPhoneNumber() == null ? appUser.getPhoneNumber() : request.getPhoneNumber());
//        appUser.setFirstName(request.getFirstName() == null ? appUser.getFirstName() : request.getFirstName());
//        appUser.setLastName(request.getLastName() == null ? appUser.getLastName() : request.getLastName());
//        appUser.setRole(request.getRoleId() == null ? appUser.getRole() : role);
//        userRepository.save(appUser);
//        return IAppUserDTO.AppUserDTO.from(userRepository.save(appUser));
//    }

    @Override
    @Transactional
    public IAppUserDTO createAppUser(AppUserRequest request) {
        AppRole role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", request.getRoleId()));
        if (existsByUsername(request.getUsername()) || existsByEmail(request.getEmail())) {
            throw new RuntimeException("Username or email address already in use");
        }
        PasswordConstraintValidator validator = new PasswordConstraintValidator();
        if (!validator.isValid(request.getPassword(), null)) {
            throw new BadRequestException("Invalid password");
        }

        // Sử dụng mapper để chuyển đổi request sang entity
        AppUser user = appUserMapper.toEntity(request);
        user.setRole(role);
        user.setHashPassword(passwordEncoder.encode(request.getPassword()));
        user.setUserCode(CodeRandom.generateRandomCode(28));

        return IAppUserDTO.AppUserDTO.from(userRepository.save(user));
    }

    @Override
    @Transactional
    public IAppUserDTO updateAppUser(Long userId, AppUserRequest request) {
        AppUser appUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal up = (UserPrincipal) authentication.getPrincipal();
        if (!up.getId().equals(appUser.getUserId()) &&
                appUser.getRole().getRoleName().equals(RoleName.ROLE_ADMIN.name())) {
            throw new AccessDeniedException("Cannot edit user with role admin");
        }

        AppRole role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", request.getRoleId()));
        if (!appUser.getUsername().equals(request.getUsername()) && existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username or email address already in use");
        }
        if (!appUser.getEmail().equals(request.getEmail()) && existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email address already in use");
        }
        PasswordConstraintValidator validator = new PasswordConstraintValidator();
        if (request.getPassword() != null && !validator.isValid(request.getPassword(), null)) {
            throw new BadRequestException("Invalid password");
        }

        // Sử dụng mapper để cập nhật entity từ request
        appUserMapper.updateEntityFromRequest(request, appUser);
        if (request.getPassword() != null) {
            appUser.setHashPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getRoleId() != null) {
            appUser.setRole(role);
        }
        return IAppUserDTO.AppUserDTO.from(userRepository.save(appUser));
    }

    @Override
    @Transactional
    public void deleteAppUser(Long userId) {
        AppUser appUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        if (appUser.getRole().getRoleName().trim().equals("ROLE_ADMIN")) {
            throw new BadRequestException("Cannot delete admin");
        }
        userRepository.delete(appUser);
    }


    @Override
    @Transactional
    public void changeBasicInformation(BasicAppUserInformationRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal up = (UserPrincipal) auth.getPrincipal();
        AppUser appUser = userRepository.findById(up.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", up.getId()));
        appUserMapper.updateBasicAppUserFromRequest(request, appUser);
        userRepository.save(appUser);
    }

    @Override
    public boolean checkConfirmedForChangeCriticalInformation(Long userId, HttpServletRequest request) {
        Cookie jSessionId = cookieUtils.findCookie(request.getCookies(), "JSESSIONID");

        String key = MessageFormat.format("{0}_{1}_{2}", userId,
                jSessionId != null ? jSessionId.getValue() : "temp_session",
                isConfirmedSuffix);

        return redisUtils.hasKey(key);
    }

    @Override
    public OtpParam getOtpForChangeCriticalInformation(Long userId, HttpServletRequest request) {
        Cookie jSessionId = cookieUtils.findCookie(request.getCookies(), "JSESSIONID");

        String key = MessageFormat.format("{0}_{1}_{2}", otpCriticalInformationPrefix,
                userId,
                jSessionId != null ? jSessionId.getValue() : "temp_session");

        return redisUtils.getFromRedis(key, OtpParam.class);
    }

    @Override
    public ConfirmEmailParam getEmailConfirmCriticalInformation() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal up = (UserPrincipal) auth.getPrincipal();

        String key = MessageFormat.format("{0}_{1}", up.getId(), isSendedEmailSuffix);

        return redisUtils.getFromRedis(key, ConfirmEmailParam.class);
    }

    @Override
    @Transactional
    public void createNewOtpForChangeCriticalInformation(UserPrincipal up, HttpServletRequest request) {
        Cookie jSessionId = cookieUtils.findCookie(request.getCookies(), "JSESSIONID");

        String key = MessageFormat.format("{0}_{1}_{2}", otpCriticalInformationPrefix,
                up.getId(),
                jSessionId != null ? jSessionId.getValue() : "temp_session");

        String otpCode = String.format("%06d", new Random().nextInt(1000000));

        OtpParam otp = OtpParam.builder()
                .yourOtpCode(otpCode)
                .emailAddress(up.getEmail())
                .username(up.getUserName())
                .sentAt(LocalDateTime.now())
                .build();

        redisUtils.saveToRedis(key, otp, maxOtpAttempt, TimeUnit.MINUTES);

        SenderTemplateEmailRequest emailRequest = SenderTemplateEmailRequest.builder()
                .templateId(idTemplateOtp)
                .messageVersions(List.of(
                        MessageVersion.builder()
                                .to(List.of(
                                        Recipient.builder()
                                                .email(up.getEmail())
                                                .name(up.getFirstName() + up.getLastName())
                                                .build()
                                ))
                                .params(otp)
                                .build())
                )
                .build();
        kafkaTemplate.send("email-delivery", emailRequest);
    }

    @Override
    @Transactional
    public void confirmModifyCriticalInformation(ConfirmChangeCriticalInformationRequest confirmRequest,
                                                 HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal up = (UserPrincipal) auth.getPrincipal();

        Cookie jSessionId = cookieUtils.findCookie(request.getCookies(), "JSESSIONID");

        String key = MessageFormat.format("{0}_{1}_{2}", otpCriticalInformationPrefix,
                up.getId(),
                jSessionId != null ? jSessionId.getValue() : "temp_session");

        OtpParam otpCodeResponse = redisUtils.getFromRedis(key, OtpParam.class);

        if (otpCodeResponse == null) {
            throw new BadRequestException("Error when confirm critical information");
        }
        if (!otpCodeResponse.getYourOtpCode().equals(confirmRequest.getOtpCode())) {
            throw new BadRequestException("Error when confirm critical information");
        }

        redisUtils.deleteKey(key);

        String isConfirmedKey = MessageFormat.format("{0}_{1}_{2}", up.getId(),
                jSessionId != null ? jSessionId.getValue() : "temp_session",
                isConfirmedSuffix);

        redisUtils.saveToRedis(isConfirmedKey, true, confirmDurationHours, TimeUnit.HOURS);
    }

    @Override
    @Transactional
    public void changeEmail(EmailAppUserRequest emailRequest, HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal up = (UserPrincipal) auth.getPrincipal();

        String key = MessageFormat.format("{0}_{1}", up.getId(), isSendedEmailSuffix);

        ConfirmEmailParam emailParam = getEmailConfirmCriticalInformation();

        if (emailParam != null) {
            if (StringUtils.hasText(emailParam.getNewEmail())) {
                if (emailParam.getSentAt().plusSeconds(resendEmailAfterSecs).isAfter(LocalDateTime.now())) {
                    throw new BadRequestException("Cannot change email now, please wait after 300 seconds to resend " +
                            "email again");
                }
            } else {
                throw new BadRequestException(MessageFormat.format("Cannot change email now, please wait for {0} hours",
                        changedEmailAfterHours));
            }
        }

        if (!checkConfirmedForChangeCriticalInformation(up.getId(), request)) {
            throw new AccessDeniedException("You must confirm your critical information before changing email");
        }

        if (userRepository.existsByEmail(emailRequest.getNewEmail())) {
            throw new ResourceConflictException("Email address already in use");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("id", up.getId());
        claims.put("email", emailRequest.getNewEmail());
        claims.put("jti", UUID.randomUUID().toString());

        String tokenConfirm = tokenProvider.generateToken(claims, codeExpireInMinutes * 60,
                null, codeEncrypt);

        emailParam = ConfirmEmailParam.builder()
                .userName(up.getUserName())
                .oldEmail(up.getEmail())
                .newEmail(emailRequest.getNewEmail())
                .link(MessageFormat.format("{0}{1}",
                        frontEndUrl, tokenConfirm))
                .sentAt(LocalDateTime.now())
                .build();

        SenderTemplateEmailRequest senderEmailRequest = SenderTemplateEmailRequest.builder()
                .templateId(idConfirmLink)
                .messageVersions(List.of(
                        MessageVersion.builder()
                                .to(List.of(
                                        Recipient.builder()
                                                .email(emailRequest.getNewEmail())
                                                .name(up.getFirstName() + up.getLastName())
                                                .build()
                                ))
                                .params(emailParam)
                                .build())
                )
                .build();
        kafkaTemplate.send("email-delivery", senderEmailRequest);

        redisUtils.saveToRedis(key, emailParam, changedEmailAfterHours, TimeUnit.HOURS);
    }

    @Override
    @Transactional
    public void changePassword(PasswordAppUserRequest passwordRequest, HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal up = (UserPrincipal) auth.getPrincipal();

        if (!checkConfirmedForChangeCriticalInformation(up.getId(), request)) {
            throw new AccessDeniedException("You must confirm your critical information before changing email");
        }

        AppUser au = userRepository.findById(up.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", up.getId()));

        if (StringUtils.hasText(au.getHashPassword()) &&
                !passwordEncoder.matches(passwordRequest.getOldPassword(), au.getHashPassword())) {
            throw new BadRequestException("Old password is wrong!");
        }
        au.setHashPassword(passwordEncoder.encode(passwordRequest.getNewPassword()));
        userRepository.save(au);
    }

    @Override
    @Transactional
    public void confirmKeyToChangeEmail(String token) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            UserPrincipal up = (UserPrincipal) auth.getPrincipal();
            Map<String, Object> claims = tokenProvider.extractAllClaims(token, codeEncrypt);

            Long id = Long.parseLong(claims.get("id").toString());

            if (!up.getId().equals(id)) {
                throw new RuntimeException("ERROR: User cannot permission!");
            }

            String jti = (String) claims.get("jti");

            String key = MessageFormat.format("{0}_{1}", tokenEmailConfirmPrefix, jti);

            if (redisUtils.hasKey(key)) {
                throw new RuntimeException("ERROR: Token is used");
            }

            String email = (String) claims.get("email");

            AppUser au = userRepository.findById(up.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", up.getId()));

            au.setEmail(email);
            userRepository.save(au);
            redisUtils.saveToRedis(key, true, codeExpireInMinutes, TimeUnit.MINUTES);

            String keySendAt = MessageFormat.format("{0}_{1}", up.getId(), isSendedEmailSuffix);
            ConfirmEmailParam confirmEmailParam = redisUtils.getFromRedis(keySendAt, ConfirmEmailParam.class);

            if (confirmEmailParam != null) {
                LocalDateTime lastSent = confirmEmailParam.getSentAt();
                confirmEmailParam.setNewEmail("");
                long newTiming = Duration.between(LocalDateTime.now(),
                        lastSent.plusHours(changedEmailAfterHours)).getSeconds();
                redisUtils.saveToRedis(keySendAt, confirmEmailParam, newTiming, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException("Cannot handle change email!");
        }
    }
}
