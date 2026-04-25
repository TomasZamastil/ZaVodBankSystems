package com.example.zavodbanksystems.databasemodel;

import jakarta.persistence.*;

@Entity
@Table(name = "Account_User")
@IdClass(AccountUserId.class)
public class AccountUser {

    @Id
    @ManyToOne
    @JoinColumn(name = "Account_idAccount")
    private Account account;

    @Id
    @ManyToOne
    @JoinColumn(name = "Client_idClient")
    private Client client;

    public AccountUser() {}

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }
}
