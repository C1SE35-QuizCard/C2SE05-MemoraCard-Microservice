package com.microservices.progressservice.repository;

import com.microservices.progressservice.model.SetFlashcard;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ISetRepository extends R2dbcRepository<SetFlashcard, Long> {
}
