package com.example.quizcards.service;

public interface ISequenceGeneratorService {
    Long getNextSequence(String collectionName, String fieldName);
}
