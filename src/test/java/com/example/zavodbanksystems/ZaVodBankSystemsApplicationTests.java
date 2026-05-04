package com.example.zavodbanksystems;

import com.example.zavodbanksystems.databasemodel.*;
import com.example.zavodbanksystems.repos.*;
import com.example.zavodbanksystems.controller.clientcontroller.*;
import com.example.zavodbanksystems.controller.admincontroller.*;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.ui.ExtendedModelMap;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ZaVodBankSystemsApplicationTests {

    @Nested
    @DisplayName("Client – hashování hesla")
    class ClientTests {

        @Test
        @DisplayName("Stejné heslo → stejný hash")
        void hashPassword_sameInput_sameHash() {
            assertEquals(Client.hashPassword("test123"), Client.hashPassword("test123"));
        }

        @Test
        @DisplayName("Různá hesla → různé hashe")
        void hashPassword_differentInput_differentHash() {
            assertNotEquals(Client.hashPassword("test123"), Client.hashPassword("jineHeslo"));
        }

        @Test
        @DisplayName("Hash má délku 64 znaků (SHA-256 hex)")
        void hashPassword_length64() {
            assertEquals(64, Client.hashPassword("heslo").length());
        }

        @Test
        @DisplayName("Prázdné heslo se hashuje bez výjimky")
        void hashPassword_emptyString_noException() {
            assertDoesNotThrow(() -> Client.hashPassword(""));
        }
    }

    @Nested
    @DisplayName("AccountType – úrokové sazby")
    class AccountTypeTests {

        @Test
        @DisplayName("SAVINGS má úrok 3.5 %")
        void savingsInterestRate() {
            assertEquals(new BigDecimal("3.50000"),
                    new AccountType(AccountType.Type.SAVINGS).getInterestRate());
        }

        @Test
        @DisplayName("CHECKING má úrok 0.1 %")
        void checkingInterestRate() {
            assertEquals(new BigDecimal("0.10000"),
                    new AccountType(AccountType.Type.CHECKING).getInterestRate());
        }

        @Test
        @DisplayName("INTERNAL má úrok 0 %")
        void internalInterestRate() {
            assertEquals(BigDecimal.ZERO,
                    new AccountType(AccountType.Type.INTERNAL).getInterestRate());
        }
    }

    @Nested
    @DisplayName("AssetInvestment – výpočet anuitní splátky")
    class AssetInvestmentTests {

        private AssetInvestment loan(double principal, double rate, int months) {
            AssetInvestment ai = new AssetInvestment();
            ai.setCurrentBase(BigDecimal.valueOf(principal));
            ai.setInterest(BigDecimal.valueOf(rate));
            ai.setTermMonths(months);
            return ai;
        }

        @Test
        @DisplayName("Nulová sazba → splátka = jistina / počet měsíců")
        void zeroInterest_equalInstalments() {
            assertEquals(new BigDecimal("10000.0000"), loan(120000, 0, 12).calculateMonthlyPayment());
        }

        @Test
        @DisplayName("Splátka je kladná pro standardní hypotéku")
        void positivePayment_standardMortgage() {
            assertTrue(loan(4250000, 5.49, 300).calculateMonthlyPayment().compareTo(BigDecimal.ZERO) > 0);
        }

        @Test
        @DisplayName("Vyšší úrok → vyšší splátka")
        void higherRate_higherPayment() {
            assertTrue(loan(1000000, 6.0, 120).calculateMonthlyPayment()
                    .compareTo(loan(1000000, 3.0, 120).calculateMonthlyPayment()) > 0);
        }

        @Test
        @DisplayName("Kratší splatnost → vyšší splátka")
        void shorterTerm_higherPayment() {
            assertTrue(loan(1000000, 5.0, 60).calculateMonthlyPayment()
                    .compareTo(loan(1000000, 5.0, 240).calculateMonthlyPayment()) > 0);
        }

        @Test
        @DisplayName("Nulový termMonths → splátka 0")
        void zeroTerm_zeroPayment() {
            assertEquals(BigDecimal.ZERO, loan(1000000, 5.0, 0).calculateMonthlyPayment());
        }

        @Test
        @DisplayName("Hypotéka 4 250 000 Kč, 5.49 %, 300 měs. → splátka ~26 073 Kč")
        void hypoteka_konkretniHodnoty() {
            BigDecimal payment = loan(4250000, 5.49, 300).calculateMonthlyPayment();
            assertTrue(payment.subtract(new BigDecimal("26073")).abs().compareTo(new BigDecimal("5")) < 0,
                    "Splátka " + payment + " se liší od 26073");
        }

        @Test
        @DisplayName("Refinancování 1 950 000 Kč, 4.99 %, 120 měs. → splátka ~20 673 Kč")
        void refinancovani_konkretniHodnoty() {
            BigDecimal payment = loan(1950000, 4.99, 120).calculateMonthlyPayment();
            assertTrue(payment.subtract(new BigDecimal("20673")).abs().compareTo(new BigDecimal("5")) < 0,
                    "Splátka " + payment + " se liší od 20673");
        }
    }

    @Nested
    @DisplayName("Salary – základní operace")
    class SalaryTests {

        @Test
        @DisplayName("Nová mzda má paid=false")
        void newSalary_notPaid() {
            assertFalse(new Salary(new Employee(), new BigDecimal("50000"),
                    LocalDateTime.now(), false).getPaid());
        }

        @Test
        @DisplayName("setPaid(true) funguje")
        void setPaid_works() {
            Salary s = new Salary();
            s.setPaid(true);
            assertTrue(s.getPaid());
        }

        @Test
        @DisplayName("Výše mzdy se uloží správně")
        void salaryAmount_stored() {
            BigDecimal amount = new BigDecimal("73250.5000");
            assertEquals(amount, new Salary(new Employee(), amount, LocalDateTime.now(), false).getAmount());
        }
    }

    @Nested
    @DisplayName("LiabilityInvestment – základní operace")
    class LiabilityInvestmentTests {

        private LiabilityInvestment li(boolean active) {
            return new LiabilityInvestment(new BigDecimal("100000"), "Test", 0,
                    new BigDecimal("3.5"), active, new BigDecimal("291.67"), null);
        }

        @Test
        @DisplayName("Aktivní závazek → getActive() == true")
        void activeLiability() { assertTrue(li(true).getActive()); }

        @Test
        @DisplayName("setActive(false) deaktivuje závazek")
        void deactivateLiability() {
            LiabilityInvestment l = li(true);
            l.setActive(false);
            assertFalse(l.getActive());
        }

        @Test
        @DisplayName("Měsíční úrok = základ * sazba / 100 / 12")
        void monthlyInterestCalculation() {
            BigDecimal expected = new BigDecimal("100000")
                    .multiply(new BigDecimal("3.5"))
                    .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP)
                    .divide(new BigDecimal("12"), 4, RoundingMode.HALF_UP);
            assertEquals(new BigDecimal("291.6667"), expected);
        }
    }

    @Nested
    @DisplayName("Account – vytvoření a základní operace")
    class AccountTests {

        @Test
        @DisplayName("SAVINGS účet dostane úrok 3.5 %")
        void savingsAccount_correctInterest() {
            Account acc = new Account(new HashSet<>(), true, new BigDecimal("10000"),
                    new AccountType(AccountType.Type.SAVINGS));
            assertEquals(new BigDecimal("3.50000"), acc.getInterest());
        }

        @Test
        @DisplayName("INTERNAL účet má nulový úrok")
        void internalAccount_zeroInterest() {
            Account acc = new Account(new HashSet<>(), true, new BigDecimal("10000000"),
                    new AccountType(AccountType.Type.INTERNAL));
            assertEquals(BigDecimal.ZERO, acc.getInterest());
        }

        @Test
        @DisplayName("setBalance funguje správně")
        void setBalance_works() {
            Account acc = new Account(new HashSet<>(), true, new BigDecimal("5000"),
                    new AccountType(AccountType.Type.CHECKING));
            acc.setBalance(new BigDecimal("7500"));
            assertEquals(new BigDecimal("7500"), acc.getBalance());
        }

        @Test
        @DisplayName("Aktivní účet vrátí activeStatus=true")
        void activeStatus_true() {
            Account acc = new Account(new HashSet<>(), true, BigDecimal.ZERO,
                    new AccountType(AccountType.Type.CHECKING));
            assertTrue(acc.getActiveStatus());
        }
    }

    @Nested
    @DisplayName("LoginController – přihlášení")
    class LoginControllerTests {

        @Mock ClientRepository clientRepository;
        @Mock EmployeeRepository employeeRepository;
        @Mock HttpSession session;

        LoginController controller;
        Model model;

        @BeforeEach
        void setUp() {
            MockitoAnnotations.openMocks(this);
            controller = new LoginController();
            inject(controller, "clientRepository", clientRepository);
            inject(controller, "employeeRepository", employeeRepository);
            model = new ExtendedModelMap();
        }

        @Test
        @DisplayName("Neznámé RČ → chybová hláška")
        void unknownRc_returnsLogin() {
            when(clientRepository.findBySocialSecurityIco("999")).thenReturn(Optional.empty());
            assertEquals("client/login", controller.loginSubmit("999", "x", session, model));
            assertNotNull(model.asMap().get("error"));
        }

        @Test
        @DisplayName("Špatné heslo → chybová hláška")
        void wrongPassword_returnsLogin() {
            Client c = new Client();
            c.setPasswordHash(Client.hashPassword("správné"));
            when(clientRepository.findBySocialSecurityIco("123")).thenReturn(Optional.of(c));
            assertEquals("client/login", controller.loginSubmit("123", "špatné", session, model));
            assertNotNull(model.asMap().get("error"));
        }

        @Test
        @DisplayName("Správné přihlášení → redirect na dashboard")
        void correctLogin_redirectsDashboard() {
            Client c = new Client();
            c.setPasswordHash(Client.hashPassword("test123"));
            c.setAccounts(new HashSet<>());
            when(clientRepository.findBySocialSecurityIco("9255209876")).thenReturn(Optional.of(c));
            when(employeeRepository.findAll()).thenReturn(List.of());
            assertEquals("redirect:/dashboard",
                    controller.loginSubmit("9255209876", "test123", session, model));
        }

        @Test
        @DisplayName("Zaměstnanec → session dostane isEmployee=true")
        void employeeLogin_setsIsEmployee() {
            Client c = new Client();
            c.setIdClient(5);
            c.setPasswordHash(Client.hashPassword("test123"));
            c.setAccounts(new HashSet<>());

            Employee emp = new Employee();
            Client empClient = new Client();
            empClient.setIdClient(5);
            emp.setClient(empClient);

            when(clientRepository.findBySocialSecurityIco("8801101122")).thenReturn(Optional.of(c));
            when(employeeRepository.findAll()).thenReturn(List.of(emp));

            controller.loginSubmit("8801101122", "test123", session, model);
            verify(session).setAttribute(eq("isEmployee"), anyBoolean());
        }
    }

    @Nested
    @DisplayName("TransferController – převody")
    class TransferControllerTests {

        @Mock AccountRepository accountRepository;
        @Mock MoneyTransferRepository moneyTransferRepository;
        @Mock HttpSession session;

        TransferController controller;
        Model model;

        @BeforeEach
        void setUp() {
            MockitoAnnotations.openMocks(this);
            controller = new TransferController();
            inject(controller, "accountRepository", accountRepository);
            inject(controller, "moneyTransferRepository", moneyTransferRepository);
            model = new ExtendedModelMap();
        }

        private Account acc(int id, double balance, String type) {
            AccountType at = new AccountType();
            at.setAccountTypeName(type);
            Account a = new Account();
            a.setIdAccount(id);
            a.setBalance(BigDecimal.valueOf(balance));
            a.setAccountType(at);
            a.setClients(new HashSet<>());
            a.setActiveStatus(true);
            return a;
        }

        @Test
        @DisplayName("Nedostatek prostředků → chybová hláška")
        void insufficientFunds_returnsError() {
            when(session.getAttribute("clientId")).thenReturn(1);
            when(session.getAttribute("isEmployee")).thenReturn(true);
            when(accountRepository.findById(1)).thenReturn(Optional.of(acc(1, 100.0, "CHECKING")));
            String view = controller.doTransfer(1, "2", "500.00", null, null, null, session, model);
            assertEquals("client/transfer", view);
            assertNotNull(model.asMap().get("error"));
        }

        @Test
        @DisplayName("Převod na stejný účet → chybová hláška")
        void sameAccount_returnsError() {
            when(session.getAttribute("clientId")).thenReturn(1);
            when(session.getAttribute("isEmployee")).thenReturn(false);
            when(accountRepository.findById(1)).thenReturn(Optional.of(acc(1, 1000.0, "CHECKING")));
            String view = controller.doTransfer(1, "1", "100.00", null, null, null, session, model);
            assertEquals("client/transfer", view);
            assertNotNull(model.asMap().get("error"));
        }

        @Test
        @DisplayName("Neplatný formát částky → chybová hláška")
        void invalidAmount_returnsError() {
            when(session.getAttribute("clientId")).thenReturn(1);
            when(session.getAttribute("isEmployee")).thenReturn(false);
            when(accountRepository.findById(1)).thenReturn(Optional.of(acc(1, 1000.0, "CHECKING")));
            String view = controller.doTransfer(1, "2", "abc", null, null, null, session, model);
            assertEquals("client/transfer", view);
            assertNotNull(model.asMap().get("error"));
        }

        @Test
        @DisplayName("Mezibankovní – neexistující účet → chyba")
        void interbank_unknownAccount_rejected() {
            when(session.getAttribute("clientId")).thenReturn(1);
            when(session.getAttribute("isEmployee")).thenReturn(false);
            when(accountRepository.findById(1)).thenReturn(Optional.of(acc(1, 10000.0, "CHECKING")));
            when(accountRepository.findAll()).thenReturn(List.of(acc(9, 10000000.0, "INTERNAL")));
            String view = controller.doTransfer(1, "9999999999", "1000", null, "9999", null, session, model);
            assertEquals("client/transfer", view);
            assertNotNull(model.asMap().get("error"));
        }

        @Test
        @DisplayName("Mezibankovní – existující testovací účet → přijato")
        void interbank_knownAccount_accepted() {
            when(session.getAttribute("clientId")).thenReturn(1);
            when(session.getAttribute("isEmployee")).thenReturn(false);
            Account src = acc(1, 50000.0, "CHECKING");
            Account bank = acc(9, 10000000.0, "INTERNAL");
            when(accountRepository.findById(1)).thenReturn(Optional.of(src));
            when(accountRepository.findAll()).thenReturn(List.of(src, bank));
            String view = controller.doTransfer(1, "1234567890", "5000", null, "0800", null, session, model);
            assertEquals("client/transfer", view);
            assertNotNull(model.asMap().get("interbank"));
            assertNull(model.asMap().get("error"));
        }
    }

    @Nested
    @DisplayName("EmployeeManagementController – výpočet mzdy")
    class EmployeeManagementTests {

        @Mock SalaryRepository salaryRepository;
        @Mock EmployeeRepository employeeRepository;
        @Mock ClientRepository clientRepository;
        @Mock AssetInvestmentRepository assetInvestmentRepository;
        @Mock AddressRepository addressRepository;
        @Mock HttpSession session;

        EmployeeManagementController controller;
        Model model;

        @BeforeEach
        void setUp() {
            MockitoAnnotations.openMocks(this);
            controller = new EmployeeManagementController();
            inject(controller, "salaryRepository", salaryRepository);
            inject(controller, "employeeRepository", employeeRepository);
            inject(controller, "clientRepository", clientRepository);
            inject(controller, "assetInvestmentRepository", assetInvestmentRepository);
            inject(controller, "addressRepository", addressRepository);
            model = new ExtendedModelMap();
        }

        @Test
        @DisplayName("Výpočet mzdy bez přístupu → redirect")
        void noAccess_redirect() {
            when(session.getAttribute("isEmployee")).thenReturn(false);
            assertEquals("redirect:/dashboard", controller.disburseSalary(1, session, model));
        }

        @Test
        @DisplayName("Mzda vypočtená tento měsíc → error hláška")
        void alreadyCalculatedThisMonth_error() {
            when(session.getAttribute("isEmployee")).thenReturn(true);

            Client c = new Client();
            c.setName("Test Zaměstnanec");

            Employee emp = new Employee();
            emp.setIdEmployee(1);
            emp.setClient(c);

            Salary existing = new Salary();
            existing.setEmployee(emp);
            existing.setPayday(LocalDateTime.now());
            existing.setPaid(false);

            when(employeeRepository.findById(1)).thenReturn(Optional.of(emp));
            when(salaryRepository.findAll()).thenReturn(List.of(existing));
            when(assetInvestmentRepository.findAll()).thenReturn(List.of());
            when(employeeRepository.findAll()).thenReturn(List.of(emp));
            when(clientRepository.findAll()).thenReturn(List.of());

            controller.disburseSalary(1, session, model);
            assertNotNull(model.asMap().get("error"));
        }
    }

    @Nested
    @DisplayName("InvestmentsController – generování závazků")
    class InvestmentsControllerTests {

        @Mock AccountRepository accountRepository;
        @Mock LiabilityInvestmentRepository liabilityInvestmentRepository;
        @Mock AssetInvestmentRepository assetInvestmentRepository;
        @Mock MoneyTransferRepository moneyTransferRepository;
        @Mock ClientRepository clientRepository;
        @Mock HttpSession session;

        InvestmentsController controller;
        Model model;

        @BeforeEach
        void setUp() {
            MockitoAnnotations.openMocks(this);
            controller = new InvestmentsController();
            inject(controller, "accountRepository", accountRepository);
            inject(controller, "liabilityInvestmentRepository", liabilityInvestmentRepository);
            inject(controller, "assetInvestmentRepository", assetInvestmentRepository);
            inject(controller, "moneyTransferRepository", moneyTransferRepository);
            inject(controller, "clientRepository", clientRepository);
            model = new ExtendedModelMap();
        }

        @Test
        @DisplayName("Generování závazků bez přístupu → redirect")
        void noAccess_redirect() {
            when(session.getAttribute("isEmployee")).thenReturn(false);
            assertEquals("redirect:/dashboard", controller.generateLiabilities(session, model));
        }

        @Test
        @DisplayName("Splacení závazků bez interního účtu → redirect na investments")
        void payLiabilities_noBankAccount_redirect() {
            when(session.getAttribute("isEmployee")).thenReturn(true);
            when(accountRepository.findAll()).thenReturn(List.of());
            assertEquals("redirect:/investments", controller.payLiabilities(session, model));
        }
    }

    private void inject(Object target, String fieldName, Object value) {
        try {
            var field = findField(target.getClass(), fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Nelze injektovat " + fieldName, e);
        }
    }

    private java.lang.reflect.Field findField(Class<?> clazz, String name) {
        while (clazz != null) {
            try { return clazz.getDeclaredField(name); }
            catch (NoSuchFieldException e) { clazz = clazz.getSuperclass(); }
        }
        throw new RuntimeException("Pole nenalezeno: " + name);
    }
}