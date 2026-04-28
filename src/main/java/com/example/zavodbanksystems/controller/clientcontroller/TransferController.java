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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

@Controller
public class TransferController {

    @Autowired private ClientRepository clientRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private MoneyTransferRepository moneyTransferRepository;

    @GetMapping("/transfer")
    @Transactional
    public String transfer(HttpSession session, Model model) {
        Integer clientId = (Integer) session.getAttribute("clientId");
        if (clientId == null) return "redirect:/login";

        Optional<Client> opt = clientRepository.findById(clientId);
        if (opt.isEmpty()) return "redirect:/login";

        model.addAttribute("accounts", opt.get().getAccounts());
        return "client/transfer";
    }

    @PostMapping("/transfer")
    @Transactional
    public String doTransfer(@RequestParam Integer sourceAccountId,
                             @RequestParam Integer destinationAccountId,
                             @RequestParam BigDecimal amount,
                             @RequestParam Integer variableSymbol,
                             HttpSession session, Model model) {
        Integer clientId = (Integer) session.getAttribute("clientId");
        if (clientId == null) return "redirect:/login";

        Optional<Client> clientOpt = clientRepository.findById(clientId);
        if (clientOpt.isEmpty()) return "redirect:/login";

        Set<Account> accounts = clientOpt.get().getAccounts();

        boolean ownsSource = accounts.stream()
                .anyMatch(a -> a.getIdAccount().equals(sourceAccountId));
        if (!ownsSource) {
            model.addAttribute("error", "Nemáte oprávnění k tomuto účtu.");
            model.addAttribute("accounts", accounts);
            return "client/transfer";
        }

        Optional<Account> srcOpt = accountRepository.findById(sourceAccountId);
        Optional<Account> dstOpt = accountRepository.findById(destinationAccountId);

        if (srcOpt.isEmpty() || dstOpt.isEmpty()) {
            model.addAttribute("error", "Cílový účet nenalezen.");
            model.addAttribute("accounts", accounts);
            return "client/transfer";
        }

        Account src = srcOpt.get();
        Account dst = dstOpt.get();

        if (src.getBalance().compareTo(amount) < 0) {
            model.addAttribute("error", "Nedostatek prostředků na účtu.");
            model.addAttribute("accounts", accounts);
            return "client/transfer";
        }

        src.setBalance(src.getBalance().subtract(amount));
        dst.setBalance(dst.getBalance().add(amount));
        accountRepository.save(src);
        accountRepository.save(dst);

        moneyTransferRepository.save(new MoneyTransfer(
                null, null, src, dst, null, null,
                amount, LocalDateTime.now(), variableSymbol, null));

        model.addAttribute("success", "Převod proběhl úspěšně.");
        model.addAttribute("accounts", clientOpt.get().getAccounts());
        return "client/transfer";
    }
}
