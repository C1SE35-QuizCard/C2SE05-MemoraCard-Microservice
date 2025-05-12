package com.example.quizcards.service.impl;

import com.example.quizcards.entities.KaAnswer;
import com.example.quizcards.repository.KaAnswerRepository;
import com.example.quizcards.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class KaAnswerService {

    @Autowired
    private KaAnswerRepository kaAnswerRepository;

    public void saveKaAnswer(Long questionId, Long answerId, BigDecimal completionTime, Boolean isCorrect, Long userId){
        kaAnswerRepository.insertKaAnswer(questionId,answerId,completionTime,isCorrect,userId);
    }
}
