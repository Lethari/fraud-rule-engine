package com.fraud.engine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fraud.engine.model.FraudResult;

@Repository
public interface FraudResultRepository extends JpaRepository<FraudResult, Long> {
    
}
