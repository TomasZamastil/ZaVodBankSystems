package com.example.zavodbanksystems.repos;

import com.example.zavodbanksystems.databasemodel.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {
}
