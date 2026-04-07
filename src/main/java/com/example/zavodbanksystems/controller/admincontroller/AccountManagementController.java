package com.example.zavodbanksystems.controller.admincontroller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AccountManagementController {
    @GetMapping("/accountManagement")
    public String accountManagement() {
        return "admin/accountManagement";
    }
}