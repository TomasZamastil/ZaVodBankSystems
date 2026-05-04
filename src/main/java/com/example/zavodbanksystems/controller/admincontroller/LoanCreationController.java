package com.example.zavodbanksystems.controller.admincontroller;

import com.example.zavodbanksystems.databasemodel.AssetInvestment;
import com.example.zavodbanksystems.databasemodel.Client;
import com.example.zavodbanksystems.databasemodel.Employee;
import com.example.zavodbanksystems.repos.AssetInvestmentRepository;
import com.example.zavodbanksystems.repos.ClientRepository;
import com.example.zavodbanksystems.repos.EmployeeRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Controller
public class LoanCreationController {

    @Autowired private AssetInvestmentRepository assetInvestmentRepository;
    @Autowired private ClientRepository clientRepository;
    @Autowired private EmployeeRepository employeeRepository;

    @GetMapping("/loanCreation")
    public String loanCreationForm(HttpSession session, Model model) {
        if (!Boolean.TRUE.equals(session.getAttribute("isEmployee"))) return "redirect:/dashboard";

        model.addAttribute("clients", clientRepository.findAll());
        model.addAttribute("employees", employeeRepository.findAll());
        model.addAttribute("isEmployee", Boolean.TRUE);
        model.addAttribute("isManager", Boolean.TRUE.equals(session.getAttribute("isManager")));
        return "admin/loanCreation";
    }

    @PostMapping("/loanCreation")
    public String createLoan(@RequestParam Integer clientId,
                             @RequestParam Integer employeeId,
                             @RequestParam String loanName,
                             @RequestParam String base,
                             @RequestParam String interest,
                             @RequestParam Integer termMonths,
                             @RequestParam Integer variableSymbol,
                             HttpSession session, Model model) {
        if (!Boolean.TRUE.equals(session.getAttribute("isEmployee"))) return "redirect:/dashboard";

        Optional<Client> clientOpt = clientRepository.findById(clientId);
        Optional<Employee> empOpt = employeeRepository.findById(employeeId);

        if (clientOpt.isEmpty() || empOpt.isEmpty()) {
            model.addAttribute("error", "Klient nebo správce nenalezen.");
            model.addAttribute("clients", clientRepository.findAll());
            model.addAttribute("employees", employeeRepository.findAll());
            model.addAttribute("isEmployee", Boolean.TRUE);
            model.addAttribute("isManager", Boolean.TRUE.equals(session.getAttribute("isManager")));
            return "admin/loanCreation";
        }

        boolean vsExists = assetInvestmentRepository.findAll().stream()
                .anyMatch(ai -> ai.getVariableSymbol().equals(variableSymbol));
        if (vsExists) {
            model.addAttribute("error", "Variabilní symbol " + variableSymbol + " je již použit.");
            model.addAttribute("clients", clientRepository.findAll());
            model.addAttribute("employees", employeeRepository.findAll());
            model.addAttribute("isEmployee", Boolean.TRUE);
            model.addAttribute("isManager", Boolean.TRUE.equals(session.getAttribute("isManager")));
            return "admin/loanCreation";
        }

        BigDecimal baseVal = new BigDecimal(base.trim().replace(",", "."));
        BigDecimal interestVal = new BigDecimal(interest.trim().replace(",", "."));

        AssetInvestment loan = new AssetInvestment(
                empOpt.get(), clientOpt.get(), loanName,
                baseVal, baseVal, interestVal,
                variableSymbol, true, termMonths
        );
        assetInvestmentRepository.save(loan);

        model.addAttribute("success", "Úvěr \"" + loanName + "\" byl úspěšně vytvořen. Měsíční splátka: "
                + loan.calculateMonthlyPayment().setScale(2, java.math.RoundingMode.HALF_UP) + " Kč");
        model.addAttribute("clients", clientRepository.findAll());
        model.addAttribute("employees", employeeRepository.findAll());
        model.addAttribute("isEmployee", Boolean.TRUE);
        model.addAttribute("isManager", Boolean.TRUE.equals(session.getAttribute("isManager")));
        return "admin/loanCreation";
    }
}
