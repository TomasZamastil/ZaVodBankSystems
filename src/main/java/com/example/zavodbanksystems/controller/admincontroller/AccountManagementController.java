package com.example.zavodbanksystems.controller.admincontroller;

import com.example.zavodbanksystems.databasemodel.Account;
import com.example.zavodbanksystems.databasemodel.AccountType;
import com.example.zavodbanksystems.databasemodel.Client;
import com.example.zavodbanksystems.repos.AccountRepository;
import com.example.zavodbanksystems.repos.AccountTypeRepository;
import com.example.zavodbanksystems.repos.ClientRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Controller
public class AccountManagementController {

    @Autowired private AccountRepository accountRepository;
    @Autowired private AccountTypeRepository accountTypeRepository;
    @Autowired private ClientRepository clientRepository;

    @GetMapping("/accountManagement")
    @Transactional
    public String accountManagement(HttpSession session, Model model) {
        if (!Boolean.TRUE.equals(session.getAttribute("isEmployee"))) return "redirect:/dashboard";
        model.addAttribute("accounts", accountRepository.findAll());
        model.addAttribute("accountTypes", accountTypeRepository.findAll());
        model.addAttribute("clients", clientRepository.findAll());
        model.addAttribute("isManager", session.getAttribute("isManager"));
        return "admin/accountManagement";
    }

    @PostMapping("/accountManagement")
    public String createAccount(@RequestParam Integer accountTypeId,
                                @RequestParam Integer clientId,
                                HttpSession session, Model model) {
        if (!Boolean.TRUE.equals(session.getAttribute("isEmployee"))) return "redirect:/dashboard";

        Optional<AccountType> typeOpt = accountTypeRepository.findById(accountTypeId);
        Optional<Client> clientOpt = clientRepository.findById(clientId);

        if (typeOpt.isPresent() && clientOpt.isPresent()) {
            Set<Client> clients = new HashSet<>();
            clients.add(clientOpt.get());
            accountRepository.save(new Account(clients, true, BigDecimal.ZERO, typeOpt.get()));
            model.addAttribute("success", "Účet byl úspěšně vytvořen.");
        } else {
            model.addAttribute("error", "Typ účtu nebo klient nenalezen.");
        }

        model.addAttribute("accounts", accountRepository.findAll());
        model.addAttribute("accountTypes", accountTypeRepository.findAll());
        model.addAttribute("clients", clientRepository.findAll());
        model.addAttribute("isManager", session.getAttribute("isManager"));
        return "admin/accountManagement";
    }

    @PostMapping("/accountDeposit")
    @Transactional
    public String deposit(@RequestParam Integer accountId,
                          @RequestParam BigDecimal amount,
                          HttpSession session, Model model) {
        if (!Boolean.TRUE.equals(session.getAttribute("isEmployee"))) return "redirect:/dashboard";

        Optional<Account> opt = accountRepository.findById(accountId);
        if (opt.isPresent()) {
            Account account = opt.get();
            account.setBalance(account.getBalance().add(amount));
            accountRepository.save(account);
            model.addAttribute("success", "Vklad " + amount + " Kč na účet č. " + accountId + " proběhl úspěšně.");
        } else {
            model.addAttribute("error", "Účet nenalezen.");
        }

        model.addAttribute("accounts", accountRepository.findAll());
        model.addAttribute("accountTypes", accountTypeRepository.findAll());
        model.addAttribute("clients", clientRepository.findAll());
        model.addAttribute("isManager", session.getAttribute("isManager"));
        return "admin/accountManagement";
    }
}
