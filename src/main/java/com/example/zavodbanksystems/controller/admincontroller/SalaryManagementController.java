package com.example.zavodbanksystems.controller.admincontroller;

import com.example.zavodbanksystems.databasemodel.Employee;
import com.example.zavodbanksystems.databasemodel.Salary;
import com.example.zavodbanksystems.repos.EmployeeRepository;
import com.example.zavodbanksystems.repos.SalaryRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Controller
public class SalaryManagementController {

    @Autowired private SalaryRepository salaryRepository;
    @Autowired private EmployeeRepository employeeRepository;

    @GetMapping("/salaryManagement")
    public String salaryManagement(HttpSession session, Model model) {
        if (session.getAttribute("clientId") == null) return "redirect:/login";
        model.addAttribute("salaries", salaryRepository.findAll());
        model.addAttribute("employees", employeeRepository.findAll());
        return "admin/salaryManagement";
    }

    @PostMapping("/salaryManagement")
    public String createSalary(@RequestParam Integer employeeId,
                               @RequestParam BigDecimal amount,
                               @RequestParam String payday,
                               HttpSession session, Model model) {
        if (session.getAttribute("clientId") == null) return "redirect:/login";

        Optional<Employee> empOpt = employeeRepository.findById(employeeId);
        if (empOpt.isPresent()) {
            salaryRepository.save(new Salary(empOpt.get(), amount, LocalDateTime.parse(payday)));
            model.addAttribute("success", "Mzda byla úspěšně zaznamenána.");
        } else {
            model.addAttribute("error", "Zaměstnanec nenalezen.");
        }

        model.addAttribute("salaries", salaryRepository.findAll());
        model.addAttribute("employees", employeeRepository.findAll());
        return "admin/salaryManagement";
    }
}
