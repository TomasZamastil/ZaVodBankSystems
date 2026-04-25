package com.example.zavodbanksystems.databasemodel;

import jakarta.persistence.*;

@Entity
@Table(name = "Account_Type")
public class AccountType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idAccount_Type")
    private Integer idAccountType;

    @Column(name = "account_type_name", nullable = false, unique = true, length = 45)
    private String accountTypeName;

    public Integer getIdAccountType() {
        return idAccountType;
    }

    public void setIdAccountType(Integer idAccountType) {
        this.idAccountType = idAccountType;
    }

    public String getAccountTypeName() {
        return accountTypeName;
    }

    public void setAccountTypeName(String accountTypeName) {
        this.accountTypeName = accountTypeName;
    }
}
