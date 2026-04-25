package com.example.zavodbanksystems.databasemodel;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "Account_Type")
public class AccountType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idAccount_Type")
    private Integer idAccountType;

    @Column(name = "account_type_name", nullable = false, unique = true, length = 45)
    private String accountTypeName;

    enum Type {
        SAVINGS, CHECKING, INTERNAL
    }

    public AccountType() {
    }

    public AccountType(Type type) {
        this.accountTypeName = type.name();
    }

    public BigDecimal getInterestRate() {
        return switch (accountTypeName) {
            case "SAVINGS" -> BigDecimal.valueOf(0.1);
            case "CHECKING" -> BigDecimal.valueOf(0.01);
            case "INTERNAL" -> BigDecimal.valueOf(0.05);
            default -> BigDecimal.ZERO;
        };
    }

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
