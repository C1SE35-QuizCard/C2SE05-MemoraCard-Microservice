package com.example.quizcards.service.impl;

import com.example.quizcards.dto.StreakStatus;
import com.example.quizcards.dto.response.StreakAnalysisResponse;
import com.example.quizcards.entities.AppUser;
import com.example.quizcards.entities.StreakAnalysis;
import com.example.quizcards.entities.StreakDetails;
import com.example.quizcards.exception.BadRequestException;
import com.example.quizcards.repository.IStreakAnalysisRepository;
import com.example.quizcards.repository.IStreakDetailsRepository;
import com.example.quizcards.utils.IbmTzLocaleUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.*;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class StreakServiceImplV2 {

}
