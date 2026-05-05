package com.example.zavodbanksystems.controller.admincontroller;

import com.example.zavodbanksystems.databasemodel.*;
import com.example.zavodbanksystems.repos.*;
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
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Controller
public class EmployeeManagementController {

    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private ClientRepository clientRepository;
    @Autowired private AddressRepository addressRepository;
    @Autowired private AssetInvestmentRepository assetInvestmentRepository;
    @Autowired private SalaryRepository salaryRepository;
    @Autowired private LiabilityInvestmentRepository liabilityInvestmentRepository;

    @GetMapping("/employeeManagement")
    @Transactional
    public String employeeManagement(HttpSession session, Model model) {
        if (!Boolean.TRUE.equals(session.getAttribute("isEmployee"))) return "redirect:/dashboard";
        loadModel(model, session);
        return "admin/employeeManagement";
    }

    @PostMapping("/employeeManagement/create")
    public String createEmployee(@RequestParam Integer clientId,
                                 @RequestParam String position,
                                 @RequestParam String basePay,
                                 @RequestParam(required = false) String bonus,
                                 @RequestParam String city,
                                 @RequestParam String postalCode,
                                 @RequestParam String street,
                                 @RequestParam String buildingNumber,
                                 @RequestParam(required = false) String apartmentNumber,
                                 HttpSession session, Model model) {
        if (!Boolean.TRUE.equals(session.getAttribute("isEmployee"))) return "redirect:/dashboard";

        Optional<Client> clientOpt = clientRepository.findById(clientId);
        if (clientOpt.isPresent()) {
            boolean alreadyEmployee = employeeRepository.findAll().stream()
                    .anyMatch(e -> e.getClient().getIdClient().equals(clientId));
            if (alreadyEmployee) {
                model.addAttribute("error", "Tento klient je již evidován jako zaměstnanec.");
                loadModel(model, session);
                return "admin/employeeManagement";
            }
            Address address = new Address(city, postalCode, street,
                    Integer.parseInt(buildingNumber.trim()),
                    apartmentNumber != null && !apartmentNumber.isBlank()
                            ? Integer.parseInt(apartmentNumber.trim()) : null);
            addressRepository.save(address);
            Client client = clientOpt.get();
            BigDecimal basePayVal = new BigDecimal(basePay.trim().replace(",", "."));
            BigDecimal bonusVal = (bonus != null && !bonus.isBlank())
                    ? new BigDecimal(bonus.trim().replace(",", ".")) : null;
            employeeRepository.save(new Employee(address, client.getSocialSecurityIco(),
                    position, basePayVal, LocalDateTime.now(), bonusVal, BigDecimal.ZERO, client));
            model.addAttribute("success", "Zaměstnanec byl úspěšně přidán.");
        } else {
            model.addAttribute("error", "Klient nenalezen.");
        }
        loadModel(model, session);
        return "admin/employeeManagement";
    }

    @PostMapping("/employeeManagement/updatePay")
    public String updatePay(@RequestParam Integer employeeId,
                            @RequestParam String basePay,
                            @RequestParam(required = false) String bonus,
                            HttpSession session, Model model) {
        if (!Boolean.TRUE.equals(session.getAttribute("isEmployee"))) return "redirect:/dashboard";

        Optional<Employee> empOpt = employeeRepository.findById(employeeId);
        if (empOpt.isPresent()) {
            Employee emp = empOpt.get();
            emp.setBasePay(new BigDecimal(basePay.trim().replace(",", ".")));
            emp.setBonus(bonus != null && !bonus.isBlank()
                    ? new BigDecimal(bonus.trim().replace(",", ".")) : null);
            employeeRepository.save(emp);
            model.addAttribute("success", "Plat byl úspěšně aktualizován.");
        } else {
            model.addAttribute("error", "Zaměstnanec nenalezen.");
        }
        loadModel(model, session);
        return "admin/employeeManagement";
    }

    @PostMapping("/employeeManagement/disburse")
    public String disburseSalary(@RequestParam Integer employeeId,
                                 HttpSession session, Model model) {
        if (!Boolean.TRUE.equals(session.getAttribute("isEmployee"))) return "redirect:/dashboard";

        Optional<Employee> empOpt = employeeRepository.findById(employeeId);
        if (empOpt.isPresent()) {
            Employee emp = empOpt.get();
            if (alreadyCalculatedThisMonth(employeeId)) {
                model.addAttribute("error", "Mzda pro " + emp.getClient().getName()
                        + " byla v tomto měsíci již vypočtena.");
            } else {
                calculateAndSave(emp);
                model.addAttribute("success", "Mzda pro " + emp.getClient().getName()
                        + " byla vypočtena. Proveďte výplatu v záložce Mzdy.");
            }
        }
        loadModel(model, session);
        return "admin/employeeManagement";
    }

    @PostMapping("/employeeManagement/disburseAll")
    public String disburseAll(HttpSession session, Model model) {
        if (!Boolean.TRUE.equals(session.getAttribute("isEmployee"))) return "redirect:/dashboard";

        List<Employee> employees = employeeRepository.findAll();
        int calculated = 0;
        int skipped = 0;
        for (Employee emp : employees) {
            if (alreadyCalculatedThisMonth(emp.getIdEmployee())) {
                skipped++;
            } else {
                calculateAndSave(emp);
                calculated++;
            }
        }

        String msg = "Vypočteno " + calculated + " mezd.";
        if (skipped > 0) msg += " Přeskočeno " + skipped + " (již vypočteno tento měsíc).";
        model.addAttribute("success", msg);
        loadModel(model, session);
        return "admin/employeeManagement";
    }

    private boolean alreadyCalculatedThisMonth(Integer employeeId) {
        YearMonth current = YearMonth.now();
        return salaryRepository.findAll().stream()
                .anyMatch(s -> s.getEmployee().getIdEmployee().equals(employeeId)
                        && YearMonth.from(s.getPayday()).equals(current));
    }

    private void calculateAndSave(Employee emp) {
        BigDecimal commission = assetInvestmentRepository.findAll().stream()
                .filter(ai -> ai.getEmployee().getIdEmployee().equals(emp.getIdEmployee()) && ai.getActive())
                .map(ai -> ai.getCurrentBase()
                        .multiply(ai.getInterest())
                        .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        emp.setCommission(commission);
        employeeRepository.save(emp);

        BigDecimal gross = emp.getBasePay()
                .add(emp.getBonus() != null ? emp.getBonus() : BigDecimal.ZERO)
                .add(commission);
        BigDecimal net = gross.multiply(new BigDecimal("0.89")).setScale(4, RoundingMode.HALF_UP);

        salaryRepository.save(new Salary(emp, net, LocalDateTime.now(), false));

        String periodLabel = YearMonth.now().format(java.time.format.DateTimeFormatter.ofPattern("MM-yyyy"));
        String liabilityName = "Mzda - " + emp.getClient().getName() + " " + periodLabel;
        boolean alreadyExists = liabilityInvestmentRepository.findAll().stream()
                .anyMatch(li -> li.getLoanName().equals(liabilityName));
        if (!alreadyExists) {
            LiabilityInvestment salaryLiability = new LiabilityInvestment(
                    net,
                    liabilityName,
                    0,
                    BigDecimal.ZERO,
                    true,
                    net,
                    null
            );
            liabilityInvestmentRepository.save(salaryLiability);
        }
    }

    private void loadModel(Model model, HttpSession session) {
        List<Employee> employees = employeeRepository.findAll();
        employees.forEach(emp -> {
            BigDecimal commission = assetInvestmentRepository.findAll().stream()
                    .filter(ai -> ai.getEmployee().getIdEmployee().equals(emp.getIdEmployee()) && ai.getActive())
                    .map(ai -> ai.getCurrentBase()
                            .multiply(ai.getInterest())
                            .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            emp.setCommission(commission);
        });

        YearMonth current = YearMonth.now();
        List<Integer> calculatedThisMonth = salaryRepository.findAll().stream()
                .filter(s -> !Boolean.TRUE.equals(s.getPaid())
                        && YearMonth.from(s.getPayday()).equals(current))
                .map(s -> s.getEmployee().getIdEmployee())
                .toList();

        model.addAttribute("employees", employees);
        model.addAttribute("calculatedThisMonth", calculatedThisMonth);
        model.addAttribute("clients", clientRepository.findAll());
        model.addAttribute("isEmployee", Boolean.TRUE);
        model.addAttribute("isManager", Boolean.TRUE.equals(session.getAttribute("isManager")));
    }
}