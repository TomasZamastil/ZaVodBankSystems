package com.example.zavodbanksystems.controller.admincontroller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SalaryManagementController {
    @GetMapping("/salaryManagement")
    public String salaryManagement() {
        return "admin/salaryManagement";
    }
}