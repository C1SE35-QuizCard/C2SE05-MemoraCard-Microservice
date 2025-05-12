package com.example.quizcards.utils;

import com.example.quizcards.entities.KaQuestion;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;


@Converter
public class AnswerListConverter implements AttributeConverter<List<KaQuestion.Answer>, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<KaQuestion.Answer> answerList) {
        try {
            return objectMapper.writeValueAsString(answerList);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert answer list to JSON", e);
        }
    }

    @Override
    public List<KaQuestion.Answer> convertToEntityAttribute(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<KaQuestion.Answer>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert JSON to answer list", e);
        }
    }
}
