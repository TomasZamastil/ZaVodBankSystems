package com.example.zavodbanksystems.repos;

import com.example.zavodbanksystems.databasemodel.AccountUser;
import com.example.zavodbanksystems.databasemodel.AccountUserId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountUserRepository extends JpaRepository<AccountUser, AccountUserId> {
}
