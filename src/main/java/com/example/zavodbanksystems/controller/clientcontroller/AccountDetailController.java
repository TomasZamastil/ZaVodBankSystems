package com.example.zavodbanksystems.controller.clientcontroller;

import com.example.zavodbanksystems.databasemodel.Account;
import com.example.zavodbanksystems.databasemodel.Client;
import com.example.zavodbanksystems.databasemodel.MoneyTransfer;
import com.example.zavodbanksystems.repos.AccountRepository;
import com.example.zavodbanksystems.repos.ClientRepository;
import com.example.zavodbanksystems.repos.MoneyTransferRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class AccountDetailController {

    @Autowired private AccountRepository accountRepository;
    @Autowired private MoneyTransferRepository moneyTransferRepository;
    @Autowired private ClientRepository clientRepository;

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

        // Pro každý převod připrav čitelný popis protistrany
        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, String>> transferDisplayList = new ArrayList<>();
        for (MoneyTransfer t : transfers) {
            Map<String, String> display = new LinkedHashMap<>();
            display.put("date", t.getTransferDate().toString());
            display.put("amount", String.format("%,.2f Kč", t.getAmount()).replace(",", " ").replace(".", ","));
            display.put("variableSymbol", String.valueOf(t.getVariableSymbol()));
            boolean isOutgoing = t.getSourceAccount().getIdAccount().equals(id);
            display.put("direction", isOutgoing ? "out" : "in");

            // Zkontroluj jestli je to mezibankovní – parsuj JSON
            String tokenComs = t.getOutsideTokenComs();
            if (tokenComs != null && !tokenComs.isBlank()) {
                try {
                    JsonNode root = mapper.readTree(tokenComs);
                    JsonNode outgoing = root.path("outgoing");
                    JsonNode incoming = root.path("incoming");
                    if (!outgoing.isMissingNode()) {
                        String accNum = outgoing.path("target_account_number").asText("");
                        String bankCode = outgoing.path("target_bank_code").asText("");
                        String status = incoming.path("status").asText("?");
                        String recipientName = incoming.path("recipient_name").asText("");
                        String bankName = incoming.path("recipient_bank").asText("");
                        if (!accNum.isBlank()) {
                            String label = accNum + "/" + bankCode;
                            if (!recipientName.isBlank()) label += " (" + recipientName;
                            if (!bankName.isBlank()) label += ", " + bankName;
                            if (!recipientName.isBlank()) label += ")";
                            if ("REJECTED".equals(status)) label += " – ZAMÍTNUTO";
                            display.put("counterparty", label);
                            display.put("interbank", "true");
                        } else {
                            display.put("counterparty", "č. " + (isOutgoing
                                    ? t.getDestinationAccount().getIdAccount()
                                    : t.getSourceAccount().getIdAccount()));
                        }
                    } else {
                        display.put("counterparty", "č. " + (isOutgoing
                                ? t.getDestinationAccount().getIdAccount()
                                : t.getSourceAccount().getIdAccount()));
                    }
                } catch (Exception e) {
                    display.put("counterparty", "č. " + (isOutgoing
                            ? t.getDestinationAccount().getIdAccount()
                            : t.getSourceAccount().getIdAccount()));
                }
            } else {
                display.put("counterparty", "č. " + (isOutgoing
                        ? t.getDestinationAccount().getIdAccount()
                        : t.getSourceAccount().getIdAccount()));
                display.put("interbank", "false");
            }
            // Poznámka pro mzdy a závazky
            if (t.getSalary() != null) display.put("note", "výplata mzdy");
            if (t.getLiabilityInvestment() != null) display.put("note", "úrok na účtu");
            transferDisplayList.add(display);
        }

        model.addAttribute("account", account);
        model.addAttribute("transfers", transfers);
        model.addAttribute("transferDisplay", transferDisplayList);
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