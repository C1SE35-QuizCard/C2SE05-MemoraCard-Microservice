package com.example.quizcards.repository;

import com.example.quizcards.entities.CategorySubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ICategorySubscriptionRepository extends JpaRepository<CategorySubscription, Long> {
    Optional<CategorySubscription> findByName(String name);
}
