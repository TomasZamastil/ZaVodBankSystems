package com.example.zavodbanksystems.databasemodel;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
//TODO: ORM už by měl odpovídat, ještě to pak ale hoď do AI, aby zkontrolova, že to odpovídá ER diagramu
@Entity
@Table(name = "Money_Transfer")
public class MoneyTransfer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idMoneyTransfer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Salaty_idSalary")
    private Salary salary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Liability_Investment_idLiability_Investment")
    private LiabilityInvestment liabilityInvestment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_account_id", nullable = false)
    private Account sourceAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_account_id", nullable = false)
    private Account destinationAccount;

    @Column(name = "outside_target")
    private Integer outsideTarget;

    @Column(name = "outside_source")
    private Integer outsideSource;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "transfer_date", nullable = false)
    private LocalDateTime transferDate;

    @Column(name = "variable_symbol", nullable = false)
    private Integer variableSymbol;

    @Column(name = "outside_token_coms", nullable = false)
    private String outsideTokenComs;

    public Integer getIdMoneyTransfer() {
        return idMoneyTransfer;
    }

    public void setIdMoneyTransfer(Integer idMoneyTransfer) {
        this.idMoneyTransfer = idMoneyTransfer;
    }

    public Salary getSalary() {
        return salary;
    }

    public void setSalary(Salary salary) {
        this.salary = salary;
    }

    public LiabilityInvestment getLiabilityInvestment() {
        return liabilityInvestment;
    }

    public void setLiabilityInvestment(LiabilityInvestment liabilityInvestment) {
        this.liabilityInvestment = liabilityInvestment;
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

    public Integer getOutsideTarget() {
        return outsideTarget;
    }

    public void setOutsideTarget(Integer outsideTarget) {
        this.outsideTarget = outsideTarget;
    }

    public Integer getOutsideSource() {
        return outsideSource;
    }

    public void setOutsideSource(Integer outsideSource) {
        this.outsideSource = outsideSource;
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

    public String getOutsideTokenComs() {
        return outsideTokenComs;
    }

    public void setOutsideTokenComs(String outsideTokenComs) {
        this.outsideTokenComs = outsideTokenComs;
    }
}