package com.example.quizcards.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "collection", uniqueConstraints = @UniqueConstraint(columnNames = {"folder_id", "set_id"}, name = "unique_collection"))
public class Collection implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "folder_id", foreignKey = @ForeignKey(name = "fk_collection_folder"), nullable = false)
    private Folder folder;

    @ManyToOne
    @JoinColumn(name = "set_id", foreignKey = @ForeignKey(name = "fk_collection_set"), nullable = false)
    private SetFlashcard set;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}