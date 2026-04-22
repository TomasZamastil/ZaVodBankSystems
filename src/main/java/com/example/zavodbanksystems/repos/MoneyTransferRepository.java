package com.example.zavodbanksystems.repos;

import com.example.zavodbanksystems.databasemodel.MoneyTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MoneyTransferRepository extends JpaRepository<MoneyTransfer, Integer> {
}
