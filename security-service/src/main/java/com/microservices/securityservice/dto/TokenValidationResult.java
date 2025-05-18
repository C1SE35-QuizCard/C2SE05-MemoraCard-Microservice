package com.microservices.securityservice.dto;

import com.microservices.dto.security.UserInfo;

public record TokenValidationResult(UserInfo userInfo, String jti) { }