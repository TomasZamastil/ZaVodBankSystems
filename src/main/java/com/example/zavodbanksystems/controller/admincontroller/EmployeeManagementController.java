package com.example.zavodbanksystems.controller.admincontroller;

import com.example.zavodbanksystems.databasemodel.Address;
import com.example.zavodbanksystems.databasemodel.Client;
import com.example.zavodbanksystems.databasemodel.Employee;
import com.example.zavodbanksystems.repos.AddressRepository;
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
import java.time.LocalDateTime;
import java.util.Optional;

@Controller
public class EmployeeManagementController {

    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private ClientRepository clientRepository;
    @Autowired private AddressRepository addressRepository;

    @GetMapping("/employeeManagement")
    public String employeeManagement(HttpSession session, Model model) {
        if (!Boolean.TRUE.equals(session.getAttribute("isEmployee"))) return "redirect:/dashboard";
        model.addAttribute("employees", employeeRepository.findAll());
        model.addAttribute("clients", clientRepository.findAll());
        return "admin/employeeManagement";
    }

    @PostMapping("/employeeManagement")
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
                    position, basePay, LocalDateTime.now(), bonus, null, client));
            model.addAttribute("success", "Zaměstnanec byl úspěšně přidán.");
        } else {
            model.addAttribute("error", "Klient nenalezen.");
        }

        model.addAttribute("employees", employeeRepository.findAll());
        model.addAttribute("clients", clientRepository.findAll());
        return "admin/employeeManagement";
    }
}
