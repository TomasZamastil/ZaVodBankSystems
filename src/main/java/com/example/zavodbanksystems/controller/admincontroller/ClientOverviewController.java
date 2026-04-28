package com.example.zavodbanksystems.controller.admincontroller;

import com.example.zavodbanksystems.repos.ClientRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ClientOverviewController {

    @Autowired private ClientRepository clientRepository;

    @GetMapping("/clientOverview")
    public String clientOverview(HttpSession session, Model model) {
        if (session.getAttribute("clientId") == null) return "redirect:/login";
        model.addAttribute("clients", clientRepository.findAll());
        return "admin/clientOverview";
    }
}
