package com.example.zavodbanksystems.databasemodel;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Salary")
public class Salary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idSalary")
    private Integer idSalary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Employee_idEmployee", nullable = false)
    private Employee employee;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "payday", nullable = false)
    private LocalDateTime payday;

    @Column(name = "paid", nullable = false)
    private Boolean paid;

    public Salary() {}

    public Salary(Employee employee, BigDecimal amount, LocalDateTime payday, Boolean paid) {
        this.employee = employee;
        this.amount = amount;
        this.payday = payday;
        this.paid = paid;
    }

    public Integer getIdSalary() { return idSalary; }
    public void setIdSalary(Integer idSalary) { this.idSalary = idSalary; }
    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public LocalDateTime getPayday() { return payday; }
    public void setPayday(LocalDateTime payday) { this.payday = payday; }
    public Boolean getPaid() { return paid; }
    public void setPaid(Boolean paid) { this.paid = paid; }
}
