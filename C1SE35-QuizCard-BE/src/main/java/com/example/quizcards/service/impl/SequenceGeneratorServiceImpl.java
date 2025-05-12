package com.example.quizcards.service.impl;

import com.example.quizcards.entities.mongoSequence.Sequence;
import com.example.quizcards.service.ISequenceGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SequenceGeneratorServiceImpl implements ISequenceGeneratorService {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Transactional(isolation = Isolation.SERIALIZABLE) // Đảm bảo transaction Serializable
    public Long getNextSequence(String collectionName, String fieldName) {
        Query query = new Query(Criteria.where("_id").is(collectionName).and("fieldName").is(fieldName));
        Update update = new Update().inc("sequence", 1);

        Sequence sequence = mongoTemplate.findAndModify(
                query,
                update,
                Sequence.class
        );

        if (sequence == null) {
            // Nếu chưa có, tạo mới
            sequence = new Sequence(collectionName, fieldName, 1L);
            mongoTemplate.insert(sequence);
            return 1L;
        }
        return sequence.getSequence();
    }
}
