package com.example.quizcards.repository;

import com.example.quizcards.entities.TestDataPackage.TestData;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface ITestDataMongoDbRepo extends MongoRepository<TestData, Long> {
    @Query("{ 'userId': ?0, 'setId': ?1, 'endAt' : { $gt: ?2 } }")
    List<TestData> findTestNotEndedInUserAndSet(Long userId, Long setId, LocalDateTime now);

    @Query(value = "{ 'userId': ?0, 'setId': ?1, '$or': [ { 'endAt': { $gt: ?2 } }, { 'isEnded': false } ], 'isEnded': false }", fields = "{ 'testId' : 1 }")
    List<TestData> findIdTestNotEnded(Long userId, Long setId, LocalDateTime now);

    @Query(value = "{}", fields = "{ 'questions' : 0 }")
    List<TestData> findAllWithoutQuestions();

    @Query("{ 'testId': ?0 }")
    TestData findByTestId(Long testId);

    @Query("{ 'userId': ?0 }")
    List<TestData> findByUserId(Long userId);

    @Query(value = "{ 'userId': ?0 }", fields = "{'questions': 0 }")
    List<TestData> findByUserIdWithoutQuestions(Long userId);

    @Query(value = "{ 'testId': ?0 }", fields = "{ 'questions':  0 }")
    TestData findByTestIdWithoutQuestions(Long testId);

    @Query(value = "{ 'userId': ?0, 'setId' : ?1 }")
    List<TestData> findByUserIdAndSetId(Long userId, Long setId);

    @Query(value = "{ 'userId': ?0, 'setId' : ?1 }", fields = "{ 'questions':  0 }")
    List<TestData> findByUserIdAndSetIdWithoutQuestions(Long userId, Long setId);

    @Query(value = "{ 'testId': ?0, 'questions.id': ?1, 'questions.answerList': { $exists: true } }",
            fields = "{ 'questions.$': 1 }")
    TestData findMCQuestionByTestIdAndQuestionId(Long testId, Long questionId);

    @Query(value = "{ 'testId': ?0, 'questions.id': ?1, 'questions.yourAnswer': { $exists: true } }",
            fields = "{ 'questions.$': 1 }")
    TestData findESQuestionByTestIdAndQuestionId(Long testId, Long questionId);
}
