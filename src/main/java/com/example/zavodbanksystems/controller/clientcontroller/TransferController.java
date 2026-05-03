package com.example.zavodbanksystems.controller.clientcontroller;

import com.example.zavodbanksystems.databasemodel.Account;
import com.example.zavodbanksystems.databasemodel.MoneyTransfer;
import com.example.zavodbanksystems.repos.AccountRepository;
import com.example.zavodbanksystems.repos.MoneyTransferRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Controller
public class TransferController {

    @Autowired private AccountRepository accountRepository;
    @Autowired private MoneyTransferRepository moneyTransferRepository;

    @GetMapping("/transfer")
    @Transactional
    public String transfer(@RequestParam Integer sourceAccountId,
                           HttpSession session, Model model) {
        Integer clientId = (Integer) session.getAttribute("clientId");
        if (clientId == null) return "redirect:/login";

        Optional<Account> srcOpt = accountRepository.findById(sourceAccountId);
        if (srcOpt.isEmpty()) return "redirect:/dashboard";

        Account source = srcOpt.get();
        boolean isEmployee = Boolean.TRUE.equals(session.getAttribute("isEmployee"));
        boolean hasAccess = isEmployee || source.getClients().stream()
                .anyMatch(c -> c.getIdClient().equals(clientId));
        if (!hasAccess) return "redirect:/dashboard";

        model.addAttribute("sourceAccount", source);
        model.addAttribute("isEmployee", isEmployee);
        model.addAttribute("isManager", session.getAttribute("isManager"));
        return "client/transfer";
    }

    // Bez @Transactional – každý save() má vlastní transakci,
    // takže při selhání triggeru můžeme ručně rollbacknout zůstatky
    @PostMapping("/transfer")
    public String doTransfer(@RequestParam Integer sourceAccountId,
                             @RequestParam String destinationAccountId,
                             @RequestParam String amount,
                             @RequestParam(required = false) String variableSymbol,
                             HttpSession session, Model model) {
        Integer clientId = (Integer) session.getAttribute("clientId");
        if (clientId == null) return "redirect:/login";

        Optional<Account> srcOpt = accountRepository.findById(sourceAccountId);
        if (srcOpt.isEmpty()) return "redirect:/dashboard";

        Account src = srcOpt.get();

        // Parsování vstupů
        Integer dstId;
        BigDecimal amountVal;
        Integer vsym;
        try {
            dstId = Integer.parseInt(destinationAccountId.trim());
            amountVal = new BigDecimal(amount.trim().replace(",", "."));
            vsym = (variableSymbol != null && !variableSymbol.isBlank())
                    ? Integer.parseInt(variableSymbol.trim()) : 0;
        } catch (NumberFormatException e) {
            model.addAttribute("error", "Neplatný formát zadaných hodnot.");
            model.addAttribute("sourceAccount", accountRepository.findById(sourceAccountId).orElse(src));
            model.addAttribute("isEmployee", session.getAttribute("isEmployee"));
            model.addAttribute("isManager", session.getAttribute("isManager"));
            return "client/transfer";
        }

        // Kontrola zůstatku
        if (src.getBalance().compareTo(amountVal) < 0) {
            model.addAttribute("error", "Nedostatek prostředků na účtu.");
            model.addAttribute("sourceAccount", src);
            model.addAttribute("isEmployee", session.getAttribute("isEmployee"));
            model.addAttribute("isManager", session.getAttribute("isManager"));
            return "client/transfer";
        }

        Optional<Account> dstOpt = accountRepository.findById(dstId);
        if (dstOpt.isEmpty()) {
            model.addAttribute("error", "Cílový účet neexistuje nebo není aktivní.");
            model.addAttribute("sourceAccount", src);
            model.addAttribute("isEmployee", session.getAttribute("isEmployee"));
            model.addAttribute("isManager", session.getAttribute("isManager"));
            return "client/transfer";
        }

        Account dst = dstOpt.get();

        // Odečti a přičti zůstatky
        src.setBalance(src.getBalance().subtract(amountVal));
        dst.setBalance(dst.getBalance().add(amountVal));
        accountRepository.save(src);
        accountRepository.save(dst);

        try {
            // Triggery se spustí zde:
            // trg_check_recipient_exists – ověří aktivitu cílového účtu
            // trg_validate_asset_repayment_amount – ověří výši splátky úvěru
            moneyTransferRepository.save(new MoneyTransfer(
                    null, null, src, dst, null, null,
                    amountVal, LocalDateTime.now(), vsym, null));

        } catch (DataIntegrityViolationException e) {
            // Trigger selhal – ručně rollbackni zůstatky
            src.setBalance(src.getBalance().add(amountVal));
            dst.setBalance(dst.getBalance().subtract(amountVal));
            accountRepository.save(src);
            accountRepository.save(dst);

            model.addAttribute("error", extractTriggerMessage(e));
            model.addAttribute("sourceAccount", accountRepository.findById(sourceAccountId).orElse(src));
            model.addAttribute("isEmployee", session.getAttribute("isEmployee"));
            model.addAttribute("isManager", session.getAttribute("isManager"));
            return "client/transfer";
        }

        return "redirect:/account/" + sourceAccountId + "?success=transfer";
    }

    private String extractTriggerMessage(DataIntegrityViolationException e) {
        Throwable cause = e.getCause();
        while (cause != null) {
            String msg = cause.getMessage();
            if (msg != null && (msg.contains("neexistuje")
                    || msg.contains("neodpovídá")
                    || msg.contains("není aktivní"))) {
                return msg;
            }
            cause = cause.getCause();
        }
        return "Převod se nezdařil. Zkontrolujte zadané údaje.";
    }
}
