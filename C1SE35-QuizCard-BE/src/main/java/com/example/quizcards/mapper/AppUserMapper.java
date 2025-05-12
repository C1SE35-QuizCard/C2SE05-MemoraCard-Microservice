package com.example.quizcards.mapper;


import com.example.quizcards.dto.request.app_user_request.AppUserRequest;
import com.example.quizcards.dto.request.app_user_request.BasicAppUserInformationRequest;
import com.example.quizcards.dto.response.UserInfoResponse;
import com.example.quizcards.entities.AppUser;
import com.example.quizcards.security.UserPrincipal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AppUserMapper {
    // Chuyển AppUserRequest sang AppUser, bỏ qua những trường được tính toán hoặc xử lý riêng.
    @Mapping(target = "hashPassword", ignore = true)
    @Mapping(target = "userCode", ignore = true)
    @Mapping(target = "role", ignore = true)
    AppUser toEntity(AppUserRequest request);

    // Cập nhật entity từ request
    @Mapping(target = "hashPassword", ignore = true)
    @Mapping(target = "userCode", ignore = true)
    @Mapping(target = "role", ignore = true)
    void updateEntityFromRequest(AppUserRequest request, @MappingTarget AppUser user);


    void updateBasicAppUserFromRequest(BasicAppUserInformationRequest request, @MappingTarget AppUser user);


    @Mapping(target = "role", ignore = true)
    @Mapping(target = "hasPassword", ignore = true)
    UserInfoResponse toUserInfoResponse(UserPrincipal user);
}
