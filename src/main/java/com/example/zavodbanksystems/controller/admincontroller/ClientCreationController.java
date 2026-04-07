package com.example.zavodbanksystems.controller.admincontroller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ClientCreationController {
    @GetMapping("/clientCreation")
    public String clientCreation() {
        return "admin/clientCreation";
    }
}