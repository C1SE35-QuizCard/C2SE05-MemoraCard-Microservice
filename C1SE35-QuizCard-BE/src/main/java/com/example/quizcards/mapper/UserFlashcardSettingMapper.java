package com.example.quizcards.mapper;

import com.example.quizcards.dto.request.FlashcardSettingRequest;
import com.example.quizcards.dto.response.FlashcardSettingResponse;
import com.example.quizcards.entities.UserFlashcardSetting;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserFlashcardSettingMapper {
    UserFlashcardSettingMapper INSTANCE = Mappers.getMapper(UserFlashcardSettingMapper.class);

    UserFlashcardSetting toEntity(FlashcardSettingRequest request);

    FlashcardSettingResponse toFlashcardSettingResponse(UserFlashcardSetting setting);

    void updateUserFromDto(FlashcardSettingRequest dto, @MappingTarget UserFlashcardSetting setting);
}
