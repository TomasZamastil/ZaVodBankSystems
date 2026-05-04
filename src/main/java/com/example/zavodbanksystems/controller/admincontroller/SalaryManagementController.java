package com.example.zavodbanksystems.controller.admincontroller;

import com.example.zavodbanksystems.databasemodel.Account;
import com.example.zavodbanksystems.databasemodel.AccountType;
import com.example.zavodbanksystems.databasemodel.MoneyTransfer;
import com.example.zavodbanksystems.databasemodel.Salary;
import com.example.zavodbanksystems.repos.AccountRepository;
import com.example.zavodbanksystems.repos.EmployeeRepository;
import com.example.zavodbanksystems.repos.MoneyTransferRepository;
import com.example.zavodbanksystems.repos.SalaryRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class SalaryManagementController {

    @Autowired private SalaryRepository salaryRepository;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private MoneyTransferRepository moneyTransferRepository;

    @GetMapping("/salaryManagement")
    public String salaryManagement(HttpSession session, Model model) {
        if (!Boolean.TRUE.equals(session.getAttribute("isEmployee"))) return "redirect:/dashboard";
        loadModel(model, session);
        return "admin/salaryManagement";
    }

    @PostMapping("/salaryManagement/pay")
    @Transactional
    public String paySingle(@RequestParam Integer salaryId,
                            HttpSession session, Model model) {
        if (!Boolean.TRUE.equals(session.getAttribute("isEmployee"))) return "redirect:/dashboard";

        Optional<Salary> salOpt = salaryRepository.findById(salaryId);
        if (salOpt.isPresent()) {
            Salary salary = salOpt.get();
            if (!Boolean.TRUE.equals(salary.getPaid())) {
                executePayout(salary);
            }
            model.addAttribute("success", "Výplata pro " + salary.getEmployee().getClient().getName() + " provedena.");
        }

        loadModel(model, session);
        return "admin/salaryManagement";
    }

    @PostMapping("/salaryManagement/payAll")
    @Transactional
    public String payAll(HttpSession session, Model model) {
        if (!Boolean.TRUE.equals(session.getAttribute("isEmployee"))) return "redirect:/dashboard";

        List<Salary> unpaid = salaryRepository.findAll().stream()
                .filter(s -> !Boolean.TRUE.equals(s.getPaid()))
                .collect(Collectors.toList());

        for (Salary s : unpaid) {
            executePayout(s);
        }

        model.addAttribute("success", "Vyplaceno " + unpaid.size() + " mezd.");
        loadModel(model, session);
        return "admin/salaryManagement";
    }

    private void executePayout(Salary salary) {

        Optional<Account> bankAccOpt = accountRepository.findAll().stream()
                .filter(a -> a.getAccountType().getAccountTypeName()
                        .equals(AccountType.Type.INTERNAL.name()))
                .findFirst();

        Optional<Account> empAccOpt = salary.getEmployee().getClient().getAccounts().stream()
                .filter(a -> a.getAccountType().getAccountTypeName()
                        .equals(AccountType.Type.CHECKING.name()))
                .findFirst();

        if (bankAccOpt.isPresent() && empAccOpt.isPresent()) {
            Account bank = bankAccOpt.get();
            Account emp = empAccOpt.get();

            bank.setBalance(bank.getBalance().subtract(salary.getAmount()));
            emp.setBalance(emp.getBalance().add(salary.getAmount()));
            accountRepository.save(bank);
            accountRepository.save(emp);

            moneyTransferRepository.save(new MoneyTransfer(
                    salary, null, bank, emp, null, null,
                    salary.getAmount(), LocalDateTime.now(), 0, null));
        }

        salary.setPaid(true);
        salary.setPayday(LocalDateTime.now());
        salaryRepository.save(salary);
    }

    private void loadModel(Model model, HttpSession session) {
        List<Salary> allSalaries = salaryRepository.findAll();
        List<Salary> unpaidSalaries = allSalaries.stream()
                .filter(s -> !Boolean.TRUE.equals(s.getPaid()))
                .collect(Collectors.toList());

        model.addAttribute("salaries", allSalaries);
        model.addAttribute("unpaidSalaries", unpaidSalaries);
        model.addAttribute("isEmployee", Boolean.TRUE);
        model.addAttribute("isManager", Boolean.TRUE.equals(session.getAttribute("isManager")));
    }
}
