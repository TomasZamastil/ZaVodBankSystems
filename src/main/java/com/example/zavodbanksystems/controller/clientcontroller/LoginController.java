package com.example.zavodbanksystems.controller.clientcontroller;

import com.example.zavodbanksystems.databasemodel.Client;
import com.example.zavodbanksystems.repos.ClientRepository;
import com.example.zavodbanksystems.repos.EmployeeRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Optional;

@Controller
public class LoginController {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login() {
        return "client/login";
    }

    @PostMapping("/login")
    @Transactional
    public String loginSubmit(@RequestParam String socialSecurityIco,
                              @RequestParam String password,
                              HttpSession session,
                              Model model) {
        Optional<Client> result = clientRepository.findBySocialSecurityIco(socialSecurityIco);

        if (result.isEmpty()) {
            model.addAttribute("error", "Neplatné přihlašovací údaje.");
            return "client/login";
        }

        Client client = result.get();
        if (!Client.hashPassword(password).equals(client.getPasswordHash())) {
            model.addAttribute("error", "Neplatné přihlašovací údaje.");
            return "client/login";
        }

        session.setAttribute("clientId", client.getIdClient());
        session.setAttribute("clientName", client.getName());

        boolean isEmployee = employeeRepository.findAll().stream()
                .anyMatch(e -> e.getClient().getIdClient().equals(client.getIdClient()));
        session.setAttribute("isEmployee", isEmployee);

        return "redirect:/dashboard";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
