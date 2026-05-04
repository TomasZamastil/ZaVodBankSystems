package com.example.zavodbanksystems.controller.clientcontroller;

import com.example.zavodbanksystems.repos.ClientRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProfileController {

    @Autowired private ClientRepository clientRepository;

    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        Integer clientId = (Integer) session.getAttribute("clientId");
        if (clientId == null) return "redirect:/login";
        clientRepository.findById(clientId).ifPresent(c -> model.addAttribute("client", c));
        model.addAttribute("isEmployee", Boolean.TRUE.equals(session.getAttribute("isEmployee")));
        model.addAttribute("isManager", Boolean.TRUE.equals(session.getAttribute("isManager")));
        return "client/profile";
    }
}
