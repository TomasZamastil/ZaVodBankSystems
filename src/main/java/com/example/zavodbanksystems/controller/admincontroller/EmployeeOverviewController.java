package com.example.zavodbanksystems.controller.admincontroller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class EmployeeOverviewController {
    @GetMapping("/employeeOverview")
    public String employeeOverview() {
        return "admin/employeeOverview";
    }
}