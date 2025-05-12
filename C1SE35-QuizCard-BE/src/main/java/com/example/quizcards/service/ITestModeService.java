package com.example.quizcards.service;

import com.example.quizcards.dto.ITestModeDTO;

import java.util.List;

public interface ITestModeService {
    List<ITestModeDTO> findAllTestMode();
    Integer exists(Long testModeId);
    List<ITestModeDTO> findTestModeByName(String testModeName);
    ITestModeDTO findTestModeById(Long testModeId);
}
