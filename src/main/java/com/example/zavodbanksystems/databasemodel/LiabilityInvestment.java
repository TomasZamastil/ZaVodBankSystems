package com.example.zavodbanksystems.databasemodel;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "Liability_Investment")
public class LiabilityInvestment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idLiabilityInvestment;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal base;

    @Column(name = "loan_name", nullable = false, unique = true, length = 45)
    private String loanName;

    @Column(name = "outside_target")
    private Integer outsideTarget;

    @Column(nullable = false, precision = 8, scale = 5)
    private BigDecimal interest;

    @Column(nullable = false)
    private Boolean active;

    @Column(name = "current_base", nullable = false, precision = 19, scale = 4)
    private BigDecimal currentBase;

    public Integer getIdLiabilityInvestment() {
        return idLiabilityInvestment;
    }

    public void setIdLiabilityInvestment(Integer idLiabilityInvestment) {
        this.idLiabilityInvestment = idLiabilityInvestment;
    }

    public BigDecimal getBase() {
        return base;
    }

    public void setBase(BigDecimal base) {
        this.base = base;
    }

    public String getLoanName() {
        return loanName;
    }

    public void setLoanName(String loanName) {
        this.loanName = loanName;
    }

    public Integer getOutsideTarget() {
        return outsideTarget;
    }

    public void setOutsideTarget(Integer outsideTarget) {
        this.outsideTarget = outsideTarget;
    }

    public BigDecimal getInterest() {
        return interest;
    }

    public void setInterest(BigDecimal interest) {
        this.interest = interest;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public BigDecimal getCurrentBase() {
        return currentBase;
    }

    public void setCurrentBase(BigDecimal currentBase) {
        this.currentBase = currentBase;
    }
}