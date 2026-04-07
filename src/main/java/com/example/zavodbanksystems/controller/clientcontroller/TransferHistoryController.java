package com.example.zavodbanksystems.controller.clientcontroller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TransferHistoryController {
    @GetMapping("/transferHistory")
    public String transferHistory() {
        return "client/transferHistory";
    }
}