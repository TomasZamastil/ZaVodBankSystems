package com.example.zavodbanksystems.controller.clientcontroller;

import com.example.zavodbanksystems.databasemodel.Account;
import com.example.zavodbanksystems.databasemodel.Client;
import com.example.zavodbanksystems.repos.ClientRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

        List<Account> accounts = client.getAccounts().stream()
                .sorted(Comparator.comparing(Account::getIdAccount))
                .collect(Collectors.toList());

        model.addAttribute("client", client);
        model.addAttribute("accounts", accounts);
        model.addAttribute("isEmployee", Boolean.TRUE.equals(session.getAttribute("isEmployee")));
        model.addAttribute("isManager", Boolean.TRUE.equals(session.getAttribute("isManager")));
        return "client/dashboard";
    }

    @GetMapping("/inAppLoan")
    public String inAppLoan() {
        return "redirect:/investments";
    }
}
