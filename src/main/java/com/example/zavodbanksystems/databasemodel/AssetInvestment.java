package com.example.zavodbanksystems.databasemodel;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "Asset_Investment")
public class AssetInvestment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idAssetInvestment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Employee_idEmployee", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Client_idClient", nullable = false)
    private Client client;

    @Column(name = "loan_name", nullable = false, unique = true, length = 45)
    private String loanName;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal base;

    @Column(name = "current_base", nullable = false, precision = 19, scale = 4)
    private BigDecimal currentBase;

    @Column(nullable = false, precision = 8, scale = 5)
    private BigDecimal interest;

    @Column(name = "variable_symbol", nullable = false)
    private Integer variableSymbol;

    @Column(nullable = false)
    private Boolean active;

    public Integer getIdAssetInvestment() {
        return idAssetInvestment;
    }

    public void setIdAssetInvestment(Integer idAssetInvestment) {
        this.idAssetInvestment = idAssetInvestment;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public String getLoanName() {
        return loanName;
    }

    public void setLoanName(String loanName) {
        this.loanName = loanName;
    }

    public BigDecimal getBase() {
        return base;
    }

    public void setBase(BigDecimal base) {
        this.base = base;
    }

    public BigDecimal getCurrentBase() {
        return currentBase;
    }

    public void setCurrentBase(BigDecimal currentBase) {
        this.currentBase = currentBase;
    }

    public BigDecimal getInterest() {
        return interest;
    }

    public void setInterest(BigDecimal interest) {
        this.interest = interest;
    }

    public Integer getVariableSymbol() {
        return variableSymbol;
    }

    public void setVariableSymbol(Integer variableSymbol) {
        this.variableSymbol = variableSymbol;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}