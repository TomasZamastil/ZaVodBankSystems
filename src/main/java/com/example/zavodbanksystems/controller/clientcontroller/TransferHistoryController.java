package com.example.zavodbanksystems.controller.clientcontroller;

import com.example.zavodbanksystems.databasemodel.Account;
import com.example.zavodbanksystems.databasemodel.Client;
import com.example.zavodbanksystems.databasemodel.MoneyTransfer;
import com.example.zavodbanksystems.repos.ClientRepository;
import com.example.zavodbanksystems.repos.MoneyTransferRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class TransferHistoryController {

    @Autowired private ClientRepository clientRepository;
    @Autowired private MoneyTransferRepository moneyTransferRepository;

    @GetMapping("/transferHistory")
    @Transactional
    public String transferHistory(HttpSession session, Model model) {
        Integer clientId = (Integer) session.getAttribute("clientId");
        if (clientId == null) return "redirect:/login";

        Optional<Client> opt = clientRepository.findById(clientId);
        if (opt.isEmpty()) return "redirect:/login";

        Set<Account> accounts = opt.get().getAccounts();
        Set<Integer> accountIds = accounts.stream()
                .map(Account::getIdAccount)
                .collect(Collectors.toSet());

        List<MoneyTransfer> transfers = moneyTransferRepository.findAll().stream()
                .filter(t -> accountIds.contains(t.getSourceAccount().getIdAccount())
                          || accountIds.contains(t.getDestinationAccount().getIdAccount()))
                .sorted((a, b) -> b.getTransferDate().compareTo(a.getTransferDate()))
                .collect(Collectors.toList());

        model.addAttribute("transfers", transfers);
        model.addAttribute("accounts", accounts);
        return "client/transferHistory";
    }
}
