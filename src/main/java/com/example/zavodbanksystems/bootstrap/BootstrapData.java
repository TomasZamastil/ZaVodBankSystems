package com.example.zavodbanksystems.bootstrap;

import com.example.zavodbanksystems.databasemodel.*;
import com.example.zavodbanksystems.repos.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Component
public class BootstrapData implements CommandLineRunner {

    private final AddressRepository addressRepository;
    private final ClientRepository clientRepository;
    private final AccountTypeRepository accountTypeRepository;
    private final AccountRepository accountRepository;
    private final EmployeeRepository employeeRepository;
    private final SalaryRepository salaryRepository;
    private final AssetInvestmentRepository assetInvestmentRepository;
    private final LiabilityInvestmentRepository liabilityInvestmentRepository;
    private final MoneyTransferRepository moneyTransferRepository;

    public BootstrapData(AddressRepository addressRepository, ClientRepository clientRepository,
                         AccountTypeRepository accountTypeRepository, AccountRepository accountRepository,
                         EmployeeRepository employeeRepository, SalaryRepository salaryRepository,
                         AssetInvestmentRepository assetInvestmentRepository,
                         LiabilityInvestmentRepository liabilityInvestmentRepository,
                         MoneyTransferRepository moneyTransferRepository) {
        this.addressRepository = addressRepository;
        this.clientRepository = clientRepository;
        this.accountTypeRepository = accountTypeRepository;
        this.accountRepository = accountRepository;
        this.employeeRepository = employeeRepository;
        this.salaryRepository = salaryRepository;
        this.assetInvestmentRepository = assetInvestmentRepository;
        this.liabilityInvestmentRepository = liabilityInvestmentRepository;
        this.moneyTransferRepository = moneyTransferRepository;
    }

    @Override
    public void run(String... args) {
        if (clientRepository.count() > 0) return;

        AccountType bezny = new AccountType(AccountType.Type.CHECKING);
        AccountType sporici = new AccountType(AccountType.Type.SAVINGS);
        AccountType interni = new AccountType(AccountType.Type.INTERNAL);
        accountTypeRepository.save(bezny);
        accountTypeRepository.save(sporici);
        accountTypeRepository.save(interni);

        Address adr1 = new Address("Praha", "110 00", "Vodičkova", 15, 3);
        Address adr2 = new Address("Brno", "602 00", "Křenová", 42, 12);
        Address adr3 = new Address("Ostrava", "702 00", "Stodolní", 8, null);
        Address adr4 = new Address("Plzeň", "301 00", "Americká", 124, 5);
        Address adrEmp = new Address("Pardubice", "530 09", "Jiřího Potůčka", 117, 12);
        addressRepository.save(adr1);
        addressRepository.save(adr2);
        addressRepository.save(adr3);
        addressRepository.save(adr4);
        addressRepository.save(adrEmp);

        Client alena = new Client(adr1, "Alena Svobodová", "9255209876", "test123",
                "a.svobodova@gmail.com", "+420608987654", null);
        Client petr = new Client(adr2, "Petr Kučera", "7801014321", "test123",
                "kucera.p@outlook.com", null, null);
        Client gastro = new Client(adr3, "Stodolní Gastro s.r.o.", "25846321", "test123",
                "info@stodolni-gastro.cz", "+420596111222", null);
        Client textilka = new Client(adr4, "Liberecká Textilka a.s.", "44556677", "test123",
                "hr@libereckatextilka.cz", "+420485100200", null);
        Client marek = new Client(adrEmp, "Marek Benson", "8801101122", "test123",
                "benson.m@banka.cz", "+420777001002", null);
        clientRepository.save(alena);
        clientRepository.save(petr);
        clientRepository.save(gastro);
        clientRepository.save(textilka);
        clientRepository.save(marek);

        Set<Client> s1 = new HashSet<>(); s1.add(alena);
        Set<Client> s2 = new HashSet<>(); s2.add(alena);
        Set<Client> s3 = new HashSet<>(); s3.add(gastro); s3.add(alena);
        Set<Client> s4 = new HashSet<>(); s4.add(petr);
        Set<Client> s5 = new HashSet<>(); s5.add(petr); s5.add(textilka);

        Account ucet1 = new Account(s1, true, new BigDecimal("15420.50"), bezny);
        Account ucet2 = new Account(s2, true, new BigDecimal("500000.00"), sporici);
        Account ucet3 = new Account(s3, false, new BigDecimal("0.00"), bezny);
        Account ucet4 = new Account(s4, true, new BigDecimal("85600.20"), bezny);
        Account ucet5 = new Account(s5, true, new BigDecimal("125400.00"), sporici);
        accountRepository.save(ucet1);
        accountRepository.save(ucet2);
        accountRepository.save(ucet3);
        accountRepository.save(ucet4);
        accountRepository.save(ucet5);

        Employee emp = new Employee(adrEmp, "8801101122", "Manažer pobočky",
                new BigDecimal("65000.00"),
                LocalDateTime.of(2020, 5, 15, 9, 0),
                new BigDecimal("500.00"), null, marek);
        employeeRepository.save(emp);

        salaryRepository.save(new Salary(emp, new BigDecimal("65000.00"),
                LocalDateTime.of(2026, 4, 1, 8, 0)));

        assetInvestmentRepository.save(new AssetInvestment(emp, alena,
                "Hypotéka - Vodičkova",
                new BigDecimal("4500000.00"), new BigDecimal("4250000.00"),
                new BigDecimal("5.49000"), 2026001, true));
        assetInvestmentRepository.save(new AssetInvestment(emp, petr,
                "Refinancování - Americká",
                new BigDecimal("2100000.00"), new BigDecimal("1950000.00"),
                new BigDecimal("4.99000"), 2026003, true));

        liabilityInvestmentRepository.save(new LiabilityInvestment(
                new BigDecimal("15420.50"), "Úrok - Běžný účet č.1",
                0, new BigDecimal("0.01000"), true, new BigDecimal("1.54"), ucet1));
        liabilityInvestmentRepository.save(new LiabilityInvestment(
                new BigDecimal("500000.00"), "Úrok - Spořicí účet č.2",
                0, new BigDecimal("2.25000"), true, new BigDecimal("11250.00"), ucet2));
        liabilityInvestmentRepository.save(new LiabilityInvestment(
                new BigDecimal("85600.20"), "Úrok - Běžný účet č.4",
                0, new BigDecimal("0.01000"), true, new BigDecimal("8.56"), ucet4));

        moneyTransferRepository.save(new MoneyTransfer(null, null, ucet1, ucet4, null, null,
                new BigDecimal("1500.00"), LocalDateTime.of(2026, 4, 1, 10, 30), 1002026, null));
        moneyTransferRepository.save(new MoneyTransfer(null, null, ucet4, ucet1, null, null,
                new BigDecimal("250.50"), LocalDateTime.of(2026, 4, 2, 14, 15), 2026001, null));
        moneyTransferRepository.save(new MoneyTransfer(null, null, ucet2, ucet5, null, null,
                new BigDecimal("5000.00"), LocalDateTime.of(2026, 4, 3, 9, 0), 3026442, null));

        System.out.println("=== BootstrapData: databáze naplněna ===");
        System.out.println("=== Přihlášení: RČ 9255209876, heslo test123 ===");
    }
}
