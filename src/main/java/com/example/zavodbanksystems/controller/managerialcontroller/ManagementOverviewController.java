package com.example.zavodbanksystems.controller.managerialcontroller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ManagementOverviewController {
    @GetMapping("/managementOverview")
    public String managementOverview() {
        return "manager/managementOverview";
    }
}