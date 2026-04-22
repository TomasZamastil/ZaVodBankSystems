package com.example.zavodbanksystems.repos;

import com.example.zavodbanksystems.databasemodel.AssetInvestment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssetInvestmentRepository extends JpaRepository<AssetInvestment, Integer> {
}
