package com.example.quizcards.service.impl;

import com.example.quizcards.dto.ITestModeDTO;
import com.example.quizcards.repository.ITestModeRepository;
import com.example.quizcards.service.ITestModeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TestModeServiceImpl implements ITestModeService {
    @Autowired
    private ITestModeRepository testModeRepository;

    @Override
    public List<ITestModeDTO> findAllTestMode(){
        return testModeRepository.findAllTestMode();
    }

    @Override
    public Integer exists(Long testModeId){
        return testModeRepository.exists(testModeId);
    }

    @Override
    public List<ITestModeDTO> findTestModeByName(String testModeName) {
        return testModeRepository.findByTestModeName(testModeName);
    }

    @Override
    public ITestModeDTO findTestModeById(Long testModeId) {
        return testModeRepository.findByTestModeId(testModeId);
    }
}
