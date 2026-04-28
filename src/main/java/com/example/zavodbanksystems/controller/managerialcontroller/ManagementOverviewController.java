package com.example.zavodbanksystems.controller.managerialcontroller;

import com.example.zavodbanksystems.databasemodel.MoneyTransfer;
import com.example.zavodbanksystems.repos.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ManagementOverviewController {

    @Autowired private ClientRepository clientRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private MoneyTransferRepository moneyTransferRepository;

    @GetMapping("/managementOverview")
    public String managementOverview(HttpSession session, Model model) {
        if (session.getAttribute("clientId") == null) return "redirect:/login";

        List<MoneyTransfer> recentTransfers = moneyTransferRepository.findAll().stream()
                .sorted(Comparator.comparing(MoneyTransfer::getTransferDate).reversed())
                .limit(10)
                .collect(Collectors.toList());

        model.addAttribute("clientCount", clientRepository.count());
        model.addAttribute("accountCount", accountRepository.count());
        model.addAttribute("employeeCount", employeeRepository.count());
        model.addAttribute("recentTransfers", recentTransfers);
        return "manager/managementOverview";
    }
}
