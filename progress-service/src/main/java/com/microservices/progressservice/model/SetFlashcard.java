package com.microservices.progressservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("set_flashcards")
public class SetFlashcard implements Serializable {
    @Id
    @Column("set_id")
    private Long setId;

    @Column("user_id")
    private Long appUserId;

    @Column("hash_password")
    private String hashPassword;
}
