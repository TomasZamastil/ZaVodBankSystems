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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class EmployeeManagementController {

    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private ClientRepository clientRepository;
    @Autowired private AddressRepository addressRepository;
    @Autowired private SalaryRepository salaryRepository;
    @Autowired private AssetInvestmentRepository assetInvestmentRepository;

    private static final BigDecimal TAX_RATE = new BigDecimal("0.11");

    @GetMapping("/employeeManagement")
    @Transactional
    public String employeeManagement(HttpSession session, Model model) {
        if (!Boolean.TRUE.equals(session.getAttribute("isEmployee"))) return "redirect:/dashboard";
        loadModel(model, session);
        return "admin/employeeManagement";
    }

    // Přidat nového zaměstnance
    @PostMapping("/employeeManagement/create")
    public String createEmployee(@RequestParam Integer clientId,
                                 @RequestParam String position,
                                 @RequestParam BigDecimal basePay,
                                 @RequestParam(required = false) BigDecimal bonus,
                                 @RequestParam String city,
                                 @RequestParam String postalCode,
                                 @RequestParam String street,
                                 @RequestParam Integer buildingNumber,
                                 @RequestParam(required = false) Integer apartmentNumber,
                                 HttpSession session, Model model) {
        if (!Boolean.TRUE.equals(session.getAttribute("isEmployee"))) return "redirect:/dashboard";

        Optional<Client> clientOpt = clientRepository.findById(clientId);
        if (clientOpt.isPresent()) {
            Address address = new Address(city, postalCode, street, buildingNumber, apartmentNumber);
            addressRepository.save(address);
            Client client = clientOpt.get();
            employeeRepository.save(new Employee(address, client.getSocialSecurityIco(),
                    position, basePay, LocalDateTime.now(), bonus, BigDecimal.ZERO, client));
            model.addAttribute("success", "Zaměstnanec byl úspěšně přidán.");
        } else {
            model.addAttribute("error", "Klient nenalezen.");
        }

        loadModel(model, session);
        return "admin/employeeManagement";
    }

    // Změna platu a bonusu
    @PostMapping("/employeeManagement/updatePay")
    public String updatePay(@RequestParam Integer employeeId,
                            @RequestParam BigDecimal basePay,
                            @RequestParam(required = false) BigDecimal bonus,
                            HttpSession session, Model model) {
        if (!Boolean.TRUE.equals(session.getAttribute("isEmployee"))) return "redirect:/dashboard";

        Optional<Employee> empOpt = employeeRepository.findById(employeeId);
        if (empOpt.isPresent()) {
            Employee emp = empOpt.get();
            emp.setBasePay(basePay);
            emp.setBonus(bonus);
            employeeRepository.save(emp);
            model.addAttribute("success", "Plat byl úspěšně aktualizován.");
        } else {
            model.addAttribute("error", "Zaměstnanec nenalezen.");
        }

        loadModel(model, session);
        return "admin/employeeManagement";
    }

    // Výplata mzdy – vypočítá z base_pay + bonus + commission, uloží jako Salary
    @PostMapping("/employeeManagement/disburse")
    @Transactional
    public String disburseSalary(@RequestParam Integer employeeId,
                                 HttpSession session, Model model) {
        if (!Boolean.TRUE.equals(session.getAttribute("isEmployee"))) return "redirect:/dashboard";

        Optional<Employee> empOpt = employeeRepository.findById(employeeId);
        if (empOpt.isPresent()) {
            Employee emp = empOpt.get();

            // Commission = součet úroků z aktivních asset investmentů tohoto zaměstnance
            BigDecimal commission = assetInvestmentRepository.findAll().stream()
                    .filter(ai -> ai.getEmployee().getIdEmployee().equals(employeeId) && ai.getActive())
                    .map(ai -> ai.getCurrentBase()
                            .multiply(ai.getInterest())
                            .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            emp.setCommission(commission);
            employeeRepository.save(emp);

            BigDecimal gross = emp.getBasePay()
                    .add(emp.getBonus() != null ? emp.getBonus() : BigDecimal.ZERO)
                    .add(commission);
            BigDecimal net = gross.multiply(BigDecimal.ONE.subtract(TAX_RATE))
                    .setScale(4, RoundingMode.HALF_UP);

            Salary salary = new Salary(emp, net, LocalDateTime.now(), false);
            salaryRepository.save(salary);
            model.addAttribute("success", "Mzda " + net + " Kč byla naplánována k výplatě pro " + emp.getClient().getName() + ".");
        } else {
            model.addAttribute("error", "Zaměstnanec nenalezen.");
        }

        loadModel(model, session);
        return "admin/employeeManagement";
    }

    private void loadModel(Model model, HttpSession session) {
        List<Employee> employees = employeeRepository.findAll();
        // Pro každého zaměstnance dopočítej aktuální commission
        employees.forEach(emp -> {
            BigDecimal commission = assetInvestmentRepository.findAll().stream()
                    .filter(ai -> ai.getEmployee().getIdEmployee().equals(emp.getIdEmployee()) && ai.getActive())
                    .map(ai -> ai.getCurrentBase()
                            .multiply(ai.getInterest())
                            .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            emp.setCommission(commission);
        });
        model.addAttribute("employees", employees);
        model.addAttribute("clients", clientRepository.findAll());
        model.addAttribute("isEmployee", session.getAttribute("isEmployee"));
        model.addAttribute("isManager", session.getAttribute("isManager"));
    }
}
