package com.example.zavodbanksystems.controller.clientcontroller;

import com.example.zavodbanksystems.databasemodel.Account;
import com.example.zavodbanksystems.databasemodel.Client;
import com.example.zavodbanksystems.databasemodel.LiabilityInvestment;
import com.example.zavodbanksystems.repos.AccountRepository;
import com.example.zavodbanksystems.repos.AssetInvestmentRepository;
import com.example.zavodbanksystems.repos.ClientRepository;
import com.example.zavodbanksystems.repos.LiabilityInvestmentRepository;
import com.example.zavodbanksystems.repos.MoneyTransferRepository;
import com.example.zavodbanksystems.databasemodel.MoneyTransfer;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.time.LocalDateTime;
import java.util.UUID;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class InvestmentsController {

    @Autowired private AssetInvestmentRepository assetInvestmentRepository;
    @Autowired private LiabilityInvestmentRepository liabilityInvestmentRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private MoneyTransferRepository moneyTransferRepository;
    @Autowired private ClientRepository clientRepository;

    @GetMapping("/investments")
    @Transactional
    public String investments(HttpSession session, Model model) {
        Integer clientId = (Integer) session.getAttribute("clientId");
        if (clientId == null) return "redirect:/login";

        boolean isEmployee = Boolean.TRUE.equals(session.getAttribute("isEmployee"));

        if (isEmployee) {
            model.addAttribute("assetInvestments", assetInvestmentRepository.findAll());
            model.addAttribute("liabilityInvestments", liabilityInvestmentRepository.findAll());
        } else {
            model.addAttribute("assetInvestments",
                    assetInvestmentRepository.findAll().stream()
                            .filter(ai -> ai.getClient().getIdClient().equals(clientId))
                            .collect(Collectors.toList()));
            model.addAttribute("liabilityInvestments",
                    liabilityInvestmentRepository.findAll().stream()
                            .filter(li -> li.getAccount() != null &&
                                    li.getAccount().getClients().stream()
                                            .anyMatch(c -> c.getIdClient().equals(clientId)))
                            .collect(Collectors.toList()));
        }

        BigDecimal totalLiabilities = liabilityInvestmentRepository.findAll().stream()
                .filter(li -> Boolean.TRUE.equals(li.getActive()))
                .map(LiabilityInvestment::getCurrentBase)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        model.addAttribute("totalLiabilities", totalLiabilities);

        YearMonth current = YearMonth.now();
        boolean liabilitiesGeneratedThisMonth = liabilityInvestmentRepository.findAll().stream()
                .anyMatch(li -> li.getAccount() != null);
        model.addAttribute("liabilitiesExist", liabilitiesGeneratedThisMonth);

        model.addAttribute("isEmployee", isEmployee);
        model.addAttribute("isManager", Boolean.TRUE.equals(session.getAttribute("isManager")));
        return "client/investments";
    }

    @PostMapping("/investments/generateLiabilities")
    @Transactional
    public String generateLiabilities(HttpSession session, Model model) {
        if (!Boolean.TRUE.equals(session.getAttribute("isEmployee"))) return "redirect:/dashboard";

        List<Account> activeAccounts = accountRepository.findAll().stream()
                .filter(a -> Boolean.TRUE.equals(a.getActiveStatus())
                        && a.getBalance().compareTo(BigDecimal.ZERO) > 0
                        && !a.getAccountType().getAccountTypeName().equals("INTERNAL"))
                .collect(Collectors.toList());

        int created = 0;
        for (Account account : activeAccounts) {

            YearMonth currentMonth = YearMonth.now();
            String monthPattern = currentMonth.format(DateTimeFormatter.ofPattern("MM-yyyy"));

            boolean activeExists = liabilityInvestmentRepository.findAll().stream()
                    .anyMatch(li -> li.getAccount() != null
                            && li.getAccount().getIdAccount().equals(account.getIdAccount())
                            && Boolean.TRUE.equals(li.getActive())
                            && li.getLoanName().contains(monthPattern));

            boolean paidThisMonth = liabilityInvestmentRepository.findAll().stream()
                    .anyMatch(li -> li.getAccount() != null
                            && li.getAccount().getIdAccount().equals(account.getIdAccount())
                            && !Boolean.TRUE.equals(li.getActive())
                            && li.getLoanName().contains(monthPattern));

            boolean exists = activeExists || paidThisMonth;
            if (!exists) {
                BigDecimal monthlyInterest = account.getBalance()
                        .multiply(account.getInterest())
                        .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP)
                        .divide(new BigDecimal("12"), 4, RoundingMode.HALF_UP);

                String periodLabel = YearMonth.now().format(DateTimeFormatter.ofPattern("MM-yyyy"));
                String uniqueName = "Úrok - " + account.getAccountType().getAccountTypeName()
                        + " č." + account.getIdAccount() + " " + periodLabel
                        + "-" + UUID.randomUUID().toString().substring(0, 6);
                LiabilityInvestment li = new LiabilityInvestment(
                        account.getBalance(),
                        uniqueName,
                        0,
                        account.getInterest(),
                        true,
                        monthlyInterest,
                        account
                );
                liabilityInvestmentRepository.save(li);
                created++;
            }
        }

        if (created == 0) {
            model.addAttribute("success", "Všechny závazky pro tento měsíc již existují.");
        } else {
            model.addAttribute("success", "Vytvořeno " + created + " nových závazků.");
        }
        return "redirect:/investments";
    }

    @PostMapping("/investments/payLiabilities")
    @Transactional
    public String payLiabilities(HttpSession session, Model model) {
        if (!Boolean.TRUE.equals(session.getAttribute("isEmployee"))) return "redirect:/dashboard";

        Optional<Account> bankAccOpt = accountRepository.findAll().stream()
                .filter(a -> a.getAccountType().getAccountTypeName()
                        .equals("INTERNAL"))
                .findFirst();

        if (bankAccOpt.isEmpty()) {
            model.addAttribute("error", "Interní účet banky nenalezen.");
            return "redirect:/investments";
        }

        Account bankAcc = bankAccOpt.get();
        List<LiabilityInvestment> active = liabilityInvestmentRepository.findAll().stream()
                .filter(li -> Boolean.TRUE.equals(li.getActive()) && li.getAccount() != null)
                .collect(Collectors.toList());

        BigDecimal total = BigDecimal.ZERO;
        for (LiabilityInvestment li : active) {
            BigDecimal interest = li.getCurrentBase();

            Account clientAcc = li.getAccount();
            clientAcc.setBalance(clientAcc.getBalance().add(interest));
            accountRepository.save(clientAcc);

            bankAcc.setBalance(bankAcc.getBalance().subtract(interest));
            total = total.add(interest);

            moneyTransferRepository.save(new MoneyTransfer(
                    null, li, bankAcc, clientAcc, null, null,
                    interest, java.time.LocalDateTime.now(), 0, null));

            li.setActive(false);
            liabilityInvestmentRepository.save(li);
        }
        accountRepository.save(bankAcc);

        model.addAttribute("success", "Splaceno celkem " + total.setScale(2, RoundingMode.HALF_UP) + " Kč úroků.");
        return "redirect:/investments";
    }

    @PostMapping("/investments/repay")
    @Transactional
    public String repayLoan(@RequestParam Integer sourceAccountId,
                            @RequestParam Integer variableSymbol,
                            @RequestParam String amount,
                            HttpSession session, Model model) {
        Integer clientId = (Integer) session.getAttribute("clientId");
        if (clientId == null) return "redirect:/login";

        Optional<Account> srcOpt = accountRepository.findById(sourceAccountId);
        if (srcOpt.isEmpty()) {
            model.addAttribute("errorMsg", "Zdrojový účet nenalezen.");
            return "redirect:/investments";
        }

        Account src = srcOpt.get();
        BigDecimal amountVal = new BigDecimal(amount.replace(",", "."));

        boolean hasAccess = src.getClients().stream().anyMatch(c -> c.getIdClient().equals(clientId));
        if (!hasAccess) return "redirect:/dashboard";

        if (src.getBalance().compareTo(amountVal) < 0) {
            model.addAttribute("errorMsg", "Nedostatek prostředků na účtu.");
            return "redirect:/investments";
        }

        Optional<Account> bankAccOpt = accountRepository.findAll().stream()
                .filter(a -> a.getAccountType().getAccountTypeName().equals("INTERNAL"))
                .findFirst();
        if (bankAccOpt.isEmpty()) return "redirect:/investments";

        Account bankAcc = bankAccOpt.get();
        src.setBalance(src.getBalance().subtract(amountVal));
        bankAcc.setBalance(bankAcc.getBalance().add(amountVal));
        accountRepository.save(src);
        accountRepository.save(bankAcc);

        try {
            moneyTransferRepository.save(new MoneyTransfer(
                    null, null, src, bankAcc, null, null,
                    amountVal, LocalDateTime.now(), variableSymbol, null));
            model.addAttribute("successMsg", "Splátka " + amountVal.setScale(2, java.math.RoundingMode.HALF_UP) + " Kč byla provedena.");
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            src.setBalance(src.getBalance().add(amountVal));
            bankAcc.setBalance(bankAcc.getBalance().subtract(amountVal));
            accountRepository.save(src);
            accountRepository.save(bankAcc);
            model.addAttribute("errorMsg", "Splátka selhala: nesprávná výše splátky.");
        }

        return "redirect:/investments";
    }
}