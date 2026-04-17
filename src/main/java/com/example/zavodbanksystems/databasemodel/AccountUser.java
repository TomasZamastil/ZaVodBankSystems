package com.example.zavodbanksystems.databasemodel;

import jakarta.persistence.*;
//TODO: ORM už by měl odpovídat, ještě to pak ale hoď do AI, aby zkontrolova, že to odpovídá ER diagramu
@Entity
@Table(name = "Account_User")
@IdClass(AccountUserId.class)
public class AccountUser {

    @Id
    @ManyToOne
    @JoinColumn(name = "Account_idAccount", referencedColumnName = "idAccount")
    private Account account;

    @Id
    @ManyToOne
    @JoinColumn(name = "Client_idClient", referencedColumnName = "idClient")
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