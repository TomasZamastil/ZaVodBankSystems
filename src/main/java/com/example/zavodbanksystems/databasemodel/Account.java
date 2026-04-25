package com.example.zavodbanksystems.databasemodel;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Set;
//TODO: ORM už by měl odpovídat, ještě to pak ale hoď do AI, aby zkontrolova, že to odpovídá ER diagramu
@Entity
@Table(name = "Account")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idAccount")
    private Integer idAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Account_Type_idAccount_type", nullable = false)
    private AccountType accountType;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal balance;

    @Column(name = "active_status", nullable = false)
    private Boolean activeStatus;

    @Column(nullable = false, precision = 8, scale = 5)
    private BigDecimal interest;

    @ManyToMany
    @JoinTable(
            name = "Account_User",
            joinColumns = @JoinColumn(name = "Account_idAccount"),
            inverseJoinColumns = @JoinColumn(name = "Client_idClient")
    )
    private Set<Client> clients;

    public Account() {
    }

    public Account(Set<Client> clients, Boolean activeStatus, BigDecimal balance, AccountType accountType) {
        this.clients = clients;
        this.activeStatus = activeStatus;
        this.balance = balance;
        this.accountType = accountType;
        this.interest = this.accountType.getInterestRate();
    }

    public Integer getIdAccount() {
        return idAccount;
    }

    public void setIdAccount(Integer idAccount) {
        this.idAccount = idAccount;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public Boolean getActiveStatus() {
        return activeStatus;
    }

    public void setActiveStatus(Boolean activeStatus) {
        this.activeStatus = activeStatus;
    }

    public BigDecimal getInterest() {
        return interest;
    }

    public void setInterest(BigDecimal interest) {
        this.interest = interest;
    }

    public Set<Client> getClients() {
        return clients;
    }

    public void setClients(Set<Client> clients) {
        this.clients = clients;
    }
}