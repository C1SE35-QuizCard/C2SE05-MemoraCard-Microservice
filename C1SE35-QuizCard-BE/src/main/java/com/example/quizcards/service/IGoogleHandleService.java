package com.example.quizcards.service;

import com.example.quizcards.dto.GoogleInfoUser;
import com.example.quizcards.dto.request.GoogleLoginRequest;

public interface IGoogleHandleService {
    GoogleInfoUser extractDataFromCode(GoogleLoginRequest request) throws Exception;
}
