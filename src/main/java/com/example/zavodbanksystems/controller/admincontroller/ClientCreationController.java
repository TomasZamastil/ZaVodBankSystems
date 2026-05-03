package com.example.zavodbanksystems.controller.admincontroller;

import com.example.zavodbanksystems.databasemodel.Address;
import com.example.zavodbanksystems.databasemodel.Client;
import com.example.zavodbanksystems.repos.AddressRepository;
import com.example.zavodbanksystems.repos.ClientRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ClientCreationController {

    @Autowired private ClientRepository clientRepository;
    @Autowired private AddressRepository addressRepository;

    @GetMapping("/clientCreation")
    public String clientCreation(HttpSession session, Model model) {
        if (!Boolean.TRUE.equals(session.getAttribute("isEmployee"))) return "redirect:/dashboard";
        model.addAttribute("isManager", session.getAttribute("isManager"));
        return "admin/clientCreation";
    }

    @PostMapping("/clientCreation")
    public String createClient(@RequestParam String name,
                               @RequestParam String socialSecurityIco,
                               @RequestParam String password,
                               @RequestParam(required = false) String email,
                               @RequestParam(required = false) String phone,
                               @RequestParam String city,
                               @RequestParam String postalCode,
                               @RequestParam String street,
                               @RequestParam Integer buildingNumber,
                               @RequestParam(required = false) Integer apartmentNumber,
                               HttpSession session, Model model) {
        if (!Boolean.TRUE.equals(session.getAttribute("isEmployee"))) return "redirect:/dashboard";

        Address address = new Address(city, postalCode, street, buildingNumber, apartmentNumber);
        addressRepository.save(address);

        Client client = new Client(address, name, socialSecurityIco, password, email, phone, null);
        clientRepository.save(client);

        model.addAttribute("success", "Klient " + name + " byl úspěšně vytvořen.");
        model.addAttribute("isManager", session.getAttribute("isManager"));
        return "admin/clientCreation";
    }
}
