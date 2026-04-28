package com.example.zavodbanksystems.controller.clientcontroller;

import com.example.zavodbanksystems.databasemodel.Client;
import com.example.zavodbanksystems.repos.ClientRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.Optional;

@Controller
public class DashboardController {

    @Autowired
    private ClientRepository clientRepository;

    @GetMapping("/dashboard")
    @Transactional
    public String dashboard(HttpSession session, Model model) {
        Integer clientId = (Integer) session.getAttribute("clientId");
        if (clientId == null) return "redirect:/login";

        Optional<Client> opt = clientRepository.findById(clientId);
        if (opt.isEmpty()) { session.invalidate(); return "redirect:/login"; }

        Client client = opt.get();
        model.addAttribute("client", client);
        model.addAttribute("accounts", client.getAccounts());
        return "client/dashboard";
    }
}
