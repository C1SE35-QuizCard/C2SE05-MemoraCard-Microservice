package com.example.quizcards.service.impl;

import com.example.quizcards.entities.KaQuestion;
import com.example.quizcards.repository.KaQuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KaQuestionService {

    @Autowired
    private KaQuestionRepository kaQuestionRepository;

    public KaQuestion saveQuestion(KaQuestion question) {
        return kaQuestionRepository.save(question);
    }

}
