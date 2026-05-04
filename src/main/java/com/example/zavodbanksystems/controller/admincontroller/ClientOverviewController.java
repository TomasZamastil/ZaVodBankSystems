package com.example.zavodbanksystems.controller.admincontroller;

import com.example.zavodbanksystems.repos.ClientRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import com.example.zavodbanksystems.databasemodel.Client;

@Controller
public class ClientOverviewController {

    @Autowired private ClientRepository clientRepository;

    @GetMapping("/clientOverview")
    @Transactional
    public String clientOverview(HttpSession session, Model model) {
        if (!Boolean.TRUE.equals(session.getAttribute("isEmployee"))) return "redirect:/dashboard";

        List<Client> clients = clientRepository.findAll().stream()
                .sorted(Comparator.comparing(Client::getIdClient))
                .collect(Collectors.toList());

        clients.forEach(c -> c.getAccounts().size());

        model.addAttribute("clientRows", clients);
        model.addAttribute("isEmployee", Boolean.TRUE);
        model.addAttribute("isManager", Boolean.TRUE.equals(session.getAttribute("isManager")));
        return "admin/clientOverview";
    }
}
