package com.example.zavodbanksystems.controller.clientcontroller;

import com.example.zavodbanksystems.databasemodel.AssetInvestment;
import com.example.zavodbanksystems.databasemodel.Client;
import com.example.zavodbanksystems.repos.AssetInvestmentRepository;
import com.example.zavodbanksystems.repos.ClientRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class InAppLoanController {

    @Autowired private ClientRepository clientRepository;
    @Autowired private AssetInvestmentRepository assetInvestmentRepository;

    @GetMapping("/inAppLoan")
    @Transactional
    public String inAppLoan(HttpSession session, Model model) {
        Integer clientId = (Integer) session.getAttribute("clientId");
        if (clientId == null) return "redirect:/login";

        Optional<Client> opt = clientRepository.findById(clientId);
        if (opt.isEmpty()) return "redirect:/login";

        Client client = opt.get();

        List<AssetInvestment> loans = assetInvestmentRepository.findAll().stream()
                .filter(l -> l.getClient().getIdClient().equals(clientId))
                .collect(Collectors.toList());

        model.addAttribute("loans", loans);
        model.addAttribute("accounts", client.getAccounts());
        model.addAttribute("isEmployee", session.getAttribute("isEmployee"));
        return "client/inAppLoan";
    }
}
