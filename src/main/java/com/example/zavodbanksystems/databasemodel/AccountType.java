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

    public enum Type {
        SAVINGS, CHECKING, INTERNAL
    }

    public AccountType() {}

    public AccountType(Type type) {
        this.accountTypeName = type.name();
    }

    public BigDecimal getInterestRate() {
        return switch (accountTypeName) {
            case "SAVINGS"   -> new BigDecimal("3.50000");   // 3.5 % ročně – běžná spořící sazba
            case "CHECKING"  -> new BigDecimal("0.10000");   // 0.1 % ročně – běžný účet, symbolická sazba
            case "INTERNAL"  -> BigDecimal.ZERO;             // interní účet banky – bez úroku
            default          -> BigDecimal.ZERO;
        };
    }

    public Integer getIdAccountType() { return idAccountType; }
    public void setIdAccountType(Integer idAccountType) { this.idAccountType = idAccountType; }
    public String getAccountTypeName() { return accountTypeName; }
    public void setAccountTypeName(String accountTypeName) { this.accountTypeName = accountTypeName; }
}
