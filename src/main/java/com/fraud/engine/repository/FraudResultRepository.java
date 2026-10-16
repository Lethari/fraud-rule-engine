package com.fraud.engine.repository;

import com.fraud.engine.model.FraudResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FraudResultRepository extends JpaRepository<FraudResult, Long> {
    
}
