package com.example.zavodbanksystems.controller.clientcontroller;

import com.example.zavodbanksystems.databasemodel.Account;
import com.example.zavodbanksystems.databasemodel.Client;
import com.example.zavodbanksystems.databasemodel.MoneyTransfer;
import com.example.zavodbanksystems.repos.AccountRepository;
import com.example.zavodbanksystems.repos.ClientRepository;
import com.example.zavodbanksystems.repos.MoneyTransferRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class AccountDetailController {

    @Autowired private AccountRepository accountRepository;
    @Autowired private MoneyTransferRepository moneyTransferRepository;
    @Autowired private ClientRepository clientRepository;
    @Autowired private JdbcTemplate jdbcTemplate;

    @GetMapping("/account/{id}")
    @Transactional
    public String accountDetail(@PathVariable Integer id,
                                @RequestParam(required = false) String success,
                                HttpSession session, Model model) {
        Integer clientId = (Integer) session.getAttribute("clientId");
        if (clientId == null) return "redirect:/login";

        Optional<Account> opt = accountRepository.findById(id);
        if (opt.isEmpty()) return "redirect:/dashboard";

        Account account = opt.get();
        boolean isEmployee = Boolean.TRUE.equals(session.getAttribute("isEmployee"));

        boolean hasAccess = isEmployee || account.getClients().stream()
                .anyMatch(c -> c.getIdClient().equals(clientId));
        if (!hasAccess) return "redirect:/dashboard";

        List<MoneyTransfer> transfers = moneyTransferRepository.findAll().stream()
                .filter(t -> t.getSourceAccount().getIdAccount().equals(id)
                        || t.getDestinationAccount().getIdAccount().equals(id))
                .sorted(Comparator.comparing(MoneyTransfer::getTransferDate).reversed())
                .collect(Collectors.toList());

        if ("deposit".equals(success)) model.addAttribute("successMsg", "Vklad proběhl úspěšně.");
        if ("transfer".equals(success)) model.addAttribute("successMsg", "Převod proběhl úspěšně.");

        List<Map<String, Object>> transferDisplay = jdbcTemplate.queryForList(
                "SELECT * FROM v_transfer_history_ext WHERE source_account_id = ? OR destination_account_id = ?",
                id, id);

        model.addAttribute("account", account);
        model.addAttribute("transfers", transfers);
        model.addAttribute("transferDisplay", transferDisplay);
        model.addAttribute("allClients", clientRepository.findAll());
        model.addAttribute("isEmployee", isEmployee);
        model.addAttribute("isManager", Boolean.TRUE.equals(session.getAttribute("isManager")));
        return "client/accountDetail";
    }

    @PostMapping("/account/{id}/deposit")
    @Transactional
    public String deposit(@PathVariable Integer id,
                          @RequestParam String amount,
                          HttpSession session) {
        if (!Boolean.TRUE.equals(session.getAttribute("isEmployee"))) return "redirect:/dashboard";

        BigDecimal value = new BigDecimal(amount.replace(",", "."));
        accountRepository.findById(id).ifPresent(a -> {
            a.setBalance(a.getBalance().add(value));
            accountRepository.save(a);
        });
        return "redirect:/account/" + id + "?success=deposit";
    }

    @PostMapping("/account/{id}/deactivate")
    @Transactional
    public String deactivate(@PathVariable Integer id, HttpSession session) {
        if (!Boolean.TRUE.equals(session.getAttribute("isEmployee"))) return "redirect:/dashboard";

        accountRepository.findById(id).ifPresent(a -> {
            a.setActiveStatus(!a.getActiveStatus());
            accountRepository.save(a);
        });
        return "redirect:/account/" + id;
    }

    @PostMapping("/account/{id}/addOwner")
    @Transactional
    public String addOwner(@PathVariable Integer id,
                           @RequestParam Integer clientId,
                           HttpSession session) {
        if (!Boolean.TRUE.equals(session.getAttribute("isEmployee"))) return "redirect:/dashboard";

        Optional<Account> accOpt = accountRepository.findById(id);
        Optional<Client> clientOpt = clientRepository.findById(clientId);

        if (accOpt.isPresent() && clientOpt.isPresent()) {
            Account account = accOpt.get();
            account.getClients().add(clientOpt.get());
            accountRepository.save(account);
        }
        return "redirect:/account/" + id;
    }
}