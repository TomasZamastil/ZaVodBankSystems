package com.example.zavodbanksystems.databasemodel;

import java.io.Serializable;
import java.util.Objects;
//TODO: tohle je jen ID class, zkontroluj to s AI
public class AccountUserId implements Serializable {
    private Integer account;
    private Integer client;

    public AccountUserId() {}

    public Integer getAccount() {
        return account;
    }

    public void setAccount(Integer account) {
        this.account = account;
    }

    public Integer getClient() {
        return client;
    }

    public void setClient(Integer client) {
        this.client = client;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountUserId that = (AccountUserId) o;
        return Objects.equals(account, that.account) && Objects.equals(client, that.client);
    }

    @Override
    public int hashCode() {
        return Objects.hash(account, client);
    }
}