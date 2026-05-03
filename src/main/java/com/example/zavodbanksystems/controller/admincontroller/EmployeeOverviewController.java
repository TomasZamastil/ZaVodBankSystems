package com.example.zavodbanksystems.controller.admincontroller;

import com.example.zavodbanksystems.repos.EmployeeRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class EmployeeOverviewController {

    @Autowired private EmployeeRepository employeeRepository;

    @GetMapping("/employeeOverview")
    public String employeeOverview(HttpSession session, Model model) {
        if (!Boolean.TRUE.equals(session.getAttribute("isEmployee"))) return "redirect:/dashboard";
        model.addAttribute("employees", employeeRepository.findAll());
        model.addAttribute("isManager", session.getAttribute("isManager"));
        return "admin/employeeOverview";
    }
}
