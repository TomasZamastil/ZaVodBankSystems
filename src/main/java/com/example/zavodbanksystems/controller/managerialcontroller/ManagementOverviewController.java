package com.example.zavodbanksystems.controller.managerialcontroller;

import com.example.zavodbanksystems.databasemodel.Account;
import com.example.zavodbanksystems.databasemodel.AssetInvestment;
import com.example.zavodbanksystems.repos.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class ManagementOverviewController {

    @Autowired private ClientRepository clientRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private AssetInvestmentRepository assetInvestmentRepository;
    @Autowired private LiabilityInvestmentRepository liabilityInvestmentRepository;
    @Autowired private MoneyTransferRepository moneyTransferRepository;
    @Autowired private JdbcTemplate jdbcTemplate;

    @GetMapping("/managementOverview")
    @Transactional
    public String managementOverview(HttpSession session, Model model) {
        if (!Boolean.TRUE.equals(session.getAttribute("isManager"))) return "redirect:/dashboard";

        model.addAttribute("clientCount", clientRepository.count());
        model.addAttribute("accountCount", accountRepository.count());
        model.addAttribute("employeeCount", employeeRepository.count());

        List<Map<String, Object>> portfolio = jdbcTemplate.queryForList(
                "SELECT * FROM v_global_portfolio_status");
        if (!portfolio.isEmpty()) model.addAttribute("portfolio", portfolio.get(0));

        Optional<Account> bankAccOpt = accountRepository.findAll().stream()
                .filter(a -> a.getAccountType().getAccountTypeName().equals("INTERNAL"))
                .findFirst();

        if (bankAccOpt.isPresent()) {
            Integer bankAccId = bankAccOpt.get().getIdAccount();

            BigDecimal totalIncoming = moneyTransferRepository.findAll().stream()
                    .filter(t -> t.getDestinationAccount().getIdAccount().equals(bankAccId)
                            && (t.getOutsideTokenComs() == null || t.getOutsideTokenComs().isBlank()))
                    .map(t -> t.getAmount())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalOutgoing = moneyTransferRepository.findAll().stream()
                    .filter(t -> t.getSourceAccount().getIdAccount().equals(bankAccId))
                    .map(t -> t.getAmount())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal liabilityPayouts = moneyTransferRepository.findAll().stream()
                    .filter(t -> t.getSourceAccount().getIdAccount().equals(bankAccId)
                            && t.getLiabilityInvestment() != null)
                    .map(t -> t.getAmount())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal salaryPayouts = moneyTransferRepository.findAll().stream()
                    .filter(t -> t.getSourceAccount().getIdAccount().equals(bankAccId)
                            && t.getSalary() != null)
                    .map(t -> t.getAmount())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            model.addAttribute("bankBalance", bankAccOpt.get().getBalance());
            model.addAttribute("totalIncoming", totalIncoming);
            model.addAttribute("totalOutgoing", totalOutgoing);
            model.addAttribute("liabilityPayouts", liabilityPayouts);
            model.addAttribute("salaryPayouts", salaryPayouts);
            model.addAttribute("netCashFlow", totalIncoming.subtract(totalOutgoing));
        }

        List<AssetInvestment> activeLoans = assetInvestmentRepository.findAll().stream()
                .filter(ai -> Boolean.TRUE.equals(ai.getActive()))
                .collect(Collectors.toList());

        List<Map<String, Object>> loanDetails = new ArrayList<>();
        BigDecimal totalMonthlyRepayment = BigDecimal.ZERO;
        BigDecimal totalRemainingProfit = BigDecimal.ZERO;

        for (AssetInvestment ai : activeLoans) {
            BigDecimal monthly = ai.calculateMonthlyPayment();

            int remainingMonths;
            if (monthly.compareTo(BigDecimal.ZERO) > 0) {
                double r = ai.getInterest().doubleValue() / 100.0 / 12.0;
                double P = ai.getCurrentBase().doubleValue();
                double M = monthly.doubleValue();
                if (r == 0) {
                    remainingMonths = (int) Math.ceil(P / M);
                } else {
                    double val = 1.0 - (P * r / M);
                    if (val <= 0) remainingMonths = 1;
                    else remainingMonths = (int) Math.ceil(-Math.log(val) / Math.log(1 + r));
                }
            } else {
                remainingMonths = 0;
            }

            BigDecimal totalLeft = monthly.multiply(new BigDecimal(remainingMonths));
            BigDecimal profit = totalLeft.subtract(ai.getCurrentBase());

            Map<String, Object> d = new LinkedHashMap<>();
            d.put("loanName", ai.getLoanName());
            d.put("clientName", ai.getClient().getName());
            d.put("currentBase", ai.getCurrentBase());
            d.put("interest", ai.getInterest());
            d.put("termMonths", remainingMonths);
            d.put("monthlyPayment", monthly);
            d.put("totalRemaining", totalLeft);
            d.put("profit", profit);
            d.put("variableSymbol", ai.getVariableSymbol());
            loanDetails.add(d);

            totalMonthlyRepayment = totalMonthlyRepayment.add(monthly);
            totalRemainingProfit = totalRemainingProfit.add(profit);
        }

        model.addAttribute("loanDetails", loanDetails);
        model.addAttribute("totalMonthlyRepayment", totalMonthlyRepayment);
        model.addAttribute("totalRemainingProfit", totalRemainingProfit);

        BigDecimal totalActiveLiabilities = liabilityInvestmentRepository.findAll().stream()
                .filter(li -> Boolean.TRUE.equals(li.getActive()))
                .map(li -> li.getCurrentBase())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        model.addAttribute("totalActiveLiabilities", totalActiveLiabilities);

        model.addAttribute("isEmployee", Boolean.TRUE.equals(session.getAttribute("isEmployee")));
        model.addAttribute("isManager", Boolean.TRUE.equals(session.getAttribute("isManager")));
        return "manager/managementOverview";
    }
}
