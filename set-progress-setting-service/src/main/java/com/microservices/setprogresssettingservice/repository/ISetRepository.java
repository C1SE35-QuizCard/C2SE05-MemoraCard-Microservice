package com.microservices.setprogresssettingservice.repository;

import com.microservices.setprogresssettingservice.model.SetFlashcard;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ISetRepository extends R2dbcRepository<SetFlashcard, Long> {
}
