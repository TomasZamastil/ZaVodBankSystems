package com.example.zavodbanksystems.controller.admincontroller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class EmployeeManagementController {
    @GetMapping("/employeeManagement")
    public String employeeManagement() {
        return "admin/employeeManagement";
    }
}