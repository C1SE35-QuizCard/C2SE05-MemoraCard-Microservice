package com.microservices.progressservice.dto.response;

import org.springframework.data.relational.core.mapping.Column;

public record IFlashcardProgressDTO(
        @Column("set_id")               Long   setId,
        @Column("title")                String title,
        @Column("avatar")               String avatar,
        @Column("user_name")            String userName,
        @Column("card_id")              Long   cardId,
        @Column("question")             String question,
        @Column("answer")               String answer,
        @Column("progress_type")        Boolean statusProgress,
        @Column("marked_for_attention") Boolean statusMark,
        @Column("image_url")            String imageUrl,
        @Column("video_url")            String videoUrl
) {}

