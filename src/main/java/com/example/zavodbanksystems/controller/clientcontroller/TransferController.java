package com.example.zavodbanksystems.controller.clientcontroller;

import com.example.zavodbanksystems.databasemodel.Account;
import com.example.zavodbanksystems.databasemodel.MoneyTransfer;
import com.example.zavodbanksystems.repos.AccountRepository;
import com.example.zavodbanksystems.repos.MoneyTransferRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Controller
public class TransferController {

    private static final Logger log = LoggerFactory.getLogger(TransferController.class);

    @Autowired private AccountRepository accountRepository;
    @Autowired private MoneyTransferRepository moneyTransferRepository;

    private static final Map<String, String[]> TEST_ACCOUNTS = Map.of(
        "1234567890/0800", new String[]{"Jan Novák", "Česká spořitelna"},
        "9876543210/0300", new String[]{"Marie Horáková s.r.o.", "ČSOB"},
        "5555000111/0100", new String[]{"Pavel Dvořák", "Komerční banka"}
    );

    @GetMapping("/transfer")
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
        model.addAttribute("isManager", Boolean.TRUE.equals(session.getAttribute("isManager")));
        return "client/transfer";
    }

    @PostMapping("/transfer")
    public String doTransfer(@RequestParam Integer sourceAccountId,
                             @RequestParam String destinationAccountId,
                             @RequestParam String amount,
                             @RequestParam(required = false) String variableSymbol,
                             @RequestParam(required = false) String bankCode,
                             @RequestParam(required = false) String bankMessage,
                             HttpSession session, Model model) {
        Integer clientId = (Integer) session.getAttribute("clientId");
        if (clientId == null) return "redirect:/login";

        Optional<Account> srcOpt = accountRepository.findById(sourceAccountId);
        if (srcOpt.isEmpty()) return "redirect:/dashboard";
        Account src = srcOpt.get();

        BigDecimal amountVal;
        Integer vsym;
        try {
            amountVal = new BigDecimal(amount.trim().replace(",", "."));
            vsym = (variableSymbol != null && !variableSymbol.isBlank())
                    ? Integer.parseInt(variableSymbol.trim()) : 0;
        } catch (NumberFormatException e) {
            return errorBack(model, src, "Neplatný formát zadaných hodnot.", session);
        }

        if (src.getBalance().compareTo(amountVal) < 0)
            return errorBack(model, src, "Nedostatek prostředků na účtu.", session);

        String destTrimmed = destinationAccountId.trim();
        boolean isInterbank = destTrimmed.length() > 4;

        if (isInterbank) {
            return handleInterbank(src, destTrimmed, bankCode, bankMessage,
                    amountVal, vsym, sourceAccountId, session, model);
        } else {
            return handleInternal(src, destTrimmed, amountVal, vsym,
                    sourceAccountId, session, model);
        }
    }

    private String handleInternal(Account src, String destIdStr, BigDecimal amount,
                                  Integer vsym, Integer sourceAccountId,
                                  HttpSession session, Model model) {
        if (destIdStr.equals(String.valueOf(sourceAccountId)))
            return errorBack(model, src, "Nelze provést převod na stejný účet.", session);

        Optional<Account> dstOpt;
        try {
            dstOpt = accountRepository.findById(Integer.parseInt(destIdStr));
        } catch (NumberFormatException e) {
            return errorBack(model, src, "Neplatné číslo účtu.", session);
        }

        if (dstOpt.isEmpty())
            return errorBack(model, src, "Cílový účet neexistuje nebo není aktivní.", session);

        Account dst = dstOpt.get();
        src.setBalance(src.getBalance().subtract(amount));
        dst.setBalance(dst.getBalance().add(amount));
        accountRepository.save(src);
        accountRepository.save(dst);

        try {
            moneyTransferRepository.save(new MoneyTransfer(
                    null, null, src, dst, null, null,
                    amount, LocalDateTime.now(), vsym, null));
        } catch (DataIntegrityViolationException e) {
            src.setBalance(src.getBalance().add(amount));
            dst.setBalance(dst.getBalance().subtract(amount));
            accountRepository.save(src);
            accountRepository.save(dst);
            return errorBack(model, src, extractTriggerMessage(e), session);
        }

        return "redirect:/account/" + sourceAccountId + "?success=transfer";
    }

    private String handleInterbank(Account src, String targetAccount, String bankCode,
                                   String bankMessage, BigDecimal amount, Integer vsym,
                                   Integer sourceAccountId, HttpSession session, Model model) {
        if (bankCode == null || bankCode.isBlank())
            return errorBack(model, src, "Pro mezibankovní převod zadejte kód banky.", session);

        Optional<Account> bankAccOpt = accountRepository.findAll().stream()
                .filter(a -> a.getAccountType().getAccountTypeName().equals("INTERNAL"))
                .findFirst();
        if (bankAccOpt.isEmpty())
            return errorBack(model, src, "Interní účet banky nenalezen.", session);
        Account bankAcc = bankAccOpt.get();

        String transferId = UUID.randomUUID().toString();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String lookupKey = targetAccount + "/" + bankCode.trim();
        String[] recipientInfo = TEST_ACCOUNTS.getOrDefault(lookupKey,
                new String[]{"Neznámý majitel", "Neznámá banka (" + bankCode + ")"});
        boolean accountExists = TEST_ACCOUNTS.containsKey(lookupKey);

        ObjectMapper mapper = new ObjectMapper();

        ObjectNode outgoing = mapper.createObjectNode();
        outgoing.put("type", "outgoing_transfer");
        outgoing.put("transfer_id", transferId);
        outgoing.put("timestamp", timestamp);
        outgoing.put("source_bank", "ZaVod Bank");
        outgoing.put("source_account_id", sourceAccountId);
        outgoing.put("target_account_number", targetAccount);
        outgoing.put("target_bank_code", bankCode.trim());
        outgoing.put("amount", amount.toString());
        outgoing.put("currency", "CZK");
        outgoing.put("variable_symbol", vsym);
        if (bankMessage != null && !bankMessage.isBlank()) outgoing.put("message", bankMessage);
        outgoing.put("status", "SENT");

        ObjectNode incoming = mapper.createObjectNode();
        incoming.put("type", "transfer_confirmation");
        incoming.put("transfer_id", transferId);
        incoming.put("timestamp", LocalDateTime.now().plusSeconds(1)
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        incoming.put("target_account_number", targetAccount);
        incoming.put("target_bank_code", bankCode.trim());
        incoming.put("amount", amount.toString());
        incoming.put("currency", "CZK");

        if (accountExists) {
            incoming.put("status", "ACCEPTED");
            incoming.put("recipient_name", recipientInfo[0]);
            incoming.put("recipient_bank", recipientInfo[1]);
            incoming.put("recipient_bank_message", "Account exists and is active. Transfer accepted.");
        } else {
            incoming.put("status", "REJECTED");
            incoming.put("error_code", "ACCOUNT_NOT_FOUND");
            incoming.put("recipient_bank_message", "Target account does not exist or is inactive.");
        }

        try {
            log.info("\n╔══════════════════════════════════════════════╗\n"
                   + "║   MEZIBANKOVNÍ PŘEVOD – ODESLANÝ JSON        ║\n"
                   + "╚══════════════════════════════════════════════╝\n{}",
                    mapper.writerWithDefaultPrettyPrinter().writeValueAsString(outgoing));
            log.info("\n╔══════════════════════════════════════════════╗\n"
                   + "║   MEZIBANKOVNÍ PŘEVOD – PŘIJATÝ JSON         ║\n"
                   + "╚══════════════════════════════════════════════╝\n{}",
                    mapper.writerWithDefaultPrettyPrinter().writeValueAsString(incoming));
        } catch (Exception ignored) {}

        if (!accountExists) {
            return errorBack(model, src,
                    "Mezibankovní převod odmítnut: účet " + targetAccount
                    + "/" + bankCode + " neexistuje.", session);
        }

        src.setBalance(src.getBalance().subtract(amount));
        bankAcc.setBalance(bankAcc.getBalance().subtract(amount));
        accountRepository.save(src);
        accountRepository.save(bankAcc);

        try {
            ObjectNode tokenComs = mapper.createObjectNode();
            tokenComs.set("outgoing", outgoing);
            tokenComs.set("incoming", incoming);
            moneyTransferRepository.save(new MoneyTransfer(
                    null, null, src, bankAcc, vsym, null,
                    amount, LocalDateTime.now(), vsym,
                    mapper.writeValueAsString(tokenComs)));
        } catch (Exception e) {
            log.error("Chyba při ukládání mezibankovního převodu: {}", e.getMessage());
        }

        model.addAttribute("sourceAccount",
                accountRepository.findById(sourceAccountId).orElse(src));
        model.addAttribute("interbank",
                "Mezibankovní převod " + amount + " Kč → "
                + recipientInfo[0] + " (" + recipientInfo[1] + ") byl přijat.");
        model.addAttribute("isEmployee", Boolean.TRUE.equals(session.getAttribute("isEmployee")));
        model.addAttribute("isManager", Boolean.TRUE.equals(session.getAttribute("isManager")));
        return "client/transfer";
    }

    private String errorBack(Model model, Account src, String error, HttpSession session) {
        model.addAttribute("error", error);
        model.addAttribute("sourceAccount",
                accountRepository.findById(src.getIdAccount()).orElse(src));
        model.addAttribute("isEmployee", Boolean.TRUE.equals(session.getAttribute("isEmployee")));
        model.addAttribute("isManager", Boolean.TRUE.equals(session.getAttribute("isManager")));
        return "client/transfer";
    }

    private String extractTriggerMessage(DataIntegrityViolationException e) {
        Throwable cause = e.getCause();
        while (cause != null) {
            String msg = cause.getMessage();
            if (msg != null && (msg.contains("neexistuje")
                    || msg.contains("neodpovídá")
                    || msg.contains("není aktivní"))) return msg;
            cause = cause.getCause();
        }
        return "Převod se nezdařil. Zkontrolujte zadané údaje.";
    }
}
