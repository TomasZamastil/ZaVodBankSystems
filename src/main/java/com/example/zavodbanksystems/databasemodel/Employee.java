package com.example.zavodbanksystems.databasemodel;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Employee")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idEmployee")
    private Integer idEmployee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_idAddress", nullable = false)
    private Address address;

    @Column(name = "social_security", nullable = false, length = 100)
    private String socialSecurity;

    @Column(name = "position", nullable = false, length = 100)
    private String position;

    @Column(name = "base_pay", nullable = false, precision = 19, scale = 4)
    private BigDecimal basePay;

    @Column(name = "hire_date", nullable = false)
    private LocalDateTime hireDate;

    @Column(name = "bonus", precision = 19, scale = 4)
    private BigDecimal bonus;

    @Column(name = "commission", precision = 19, scale = 4)
    private BigDecimal commission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Client_idClient", nullable = false)
    private Client client;

    public Employee() {
    }

    public Employee(Address address, String socialSecurity, String position, BigDecimal basePay, LocalDateTime hireDate, BigDecimal bonus, BigDecimal commission, Client client) {
        this.address = address;
        this.socialSecurity = socialSecurity;
        this.position = position;
        this.basePay = basePay;
        this.hireDate = hireDate;
        this.bonus = bonus;
        this.commission = commission;
        this.client = client;
    }

    public Integer getIdEmployee() {
        return idEmployee;
    }

    public void setIdEmployee(Integer idEmployee) {
        this.idEmployee = idEmployee;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public String getSocialSecurity() {
        return socialSecurity;
    }

    public void setSocialSecurity(String socialSecurity) {
        this.socialSecurity = socialSecurity;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public BigDecimal getBasePay() {
        return basePay;
    }

    public void setBasePay(BigDecimal basePay) {
        this.basePay = basePay;
    }

    public LocalDateTime getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDateTime hireDate) {
        this.hireDate = hireDate;
    }

    public BigDecimal getBonus() {
        return bonus;
    }

    public void setBonus(BigDecimal bonus) {
        this.bonus = bonus;
    }

    public BigDecimal getCommission() {
        return commission;
    }

    public void setCommission(BigDecimal commission) {
        this.commission = commission;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }
}