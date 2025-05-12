package com.example.quizcards.entities.mongoSequence;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "mongo_sequence")
public class Sequence {
    @Id
    @Indexed
    private String collectionName;

    @Indexed
    private String fieldName;

    private Long sequence;
}

