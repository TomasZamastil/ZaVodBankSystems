package com.example.zavodbanksystems.controller.clientcontroller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InAppLoanController {
    @GetMapping("/inAppLoan")
    public String inAppLoan() {
        return "client/inAppLoan";
    }
}