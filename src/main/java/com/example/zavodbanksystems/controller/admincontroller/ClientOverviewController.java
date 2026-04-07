package com.example.zavodbanksystems.controller.admincontroller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ClientOverviewController {
    @GetMapping("/clientOverview")
    public String clientOverview() {
        return "admin/clientOverview";
    }
}