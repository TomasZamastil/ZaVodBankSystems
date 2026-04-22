package com.example.zavodbanksystems.repos;

import com.example.zavodbanksystems.databasemodel.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountTypeRepository extends JpaRepository<AccountType, Integer> {
}
