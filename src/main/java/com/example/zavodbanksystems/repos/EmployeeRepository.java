package com.example.zavodbanksystems.repos;

import com.example.zavodbanksystems.databasemodel.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
}
