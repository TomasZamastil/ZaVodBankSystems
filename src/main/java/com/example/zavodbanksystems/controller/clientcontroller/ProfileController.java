package com.example.zavodbanksystems.controller.clientcontroller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProfileController {
    @GetMapping("/profile")
    public String profile() {
        return "client/profile";
    }
}