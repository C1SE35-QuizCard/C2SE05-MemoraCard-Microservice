package com.microservices.progressservice.dto.response;

import org.springframework.data.relational.core.mapping.Column;

public record IProgressAnalysisDTO(
        @Column("set_id")                Long setId,
        @Column("title")                 String setTitle,
        @Column("total_card_recall")     Long totalCardRecall,
        @Column("total_card_remember")   Long totalCardRemember,
        @Column("total_card_not_learn")  Long totalCardNotLearn
) {}
