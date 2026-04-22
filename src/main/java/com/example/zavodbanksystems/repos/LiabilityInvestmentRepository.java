package com.example.zavodbanksystems.repos;

import com.example.zavodbanksystems.databasemodel.LiabilityInvestment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LiabilityInvestmentRepository extends JpaRepository<LiabilityInvestment, Integer> {
}
