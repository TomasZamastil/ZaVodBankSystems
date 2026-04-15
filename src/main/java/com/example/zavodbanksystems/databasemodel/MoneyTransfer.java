package com.example.zavodbanksystems.databasemodel;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Money_Transfer")
public class MoneyTransfer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idMoneyTransfer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_account_id", nullable = false)
    private Account sourceAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_account_id", nullable = false)
    private Account destinationAccount;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "transfer_date", nullable = false)
    private LocalDateTime transferDate;

    @Column(name = "variable_symbol", nullable = false)
    private Integer variableSymbol;

    public Integer getIdMoneyTransfer() {
        return idMoneyTransfer;
    }

    public void setIdMoneyTransfer(Integer idMoneyTransfer) {
        this.idMoneyTransfer = idMoneyTransfer;
    }

    public Account getSourceAccount() {
        return sourceAccount;
    }

    public void setSourceAccount(Account sourceAccount) {
        this.sourceAccount = sourceAccount;
    }

    public Account getDestinationAccount() {
        return destinationAccount;
    }

    public void setDestinationAccount(Account destinationAccount) {
        this.destinationAccount = destinationAccount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getTransferDate() {
        return transferDate;
    }

    public void setTransferDate(LocalDateTime transferDate) {
        this.transferDate = transferDate;
    }

    public Integer getVariableSymbol() {
        return variableSymbol;
    }

    public void setVariableSymbol(Integer variableSymbol) {
        this.variableSymbol = variableSymbol;
    }
}