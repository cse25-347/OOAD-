package bw.edu.banking;

import bw.edu.banking.model.AccountType;
import bw.edu.banking.model.Customer;
import bw.edu.banking.controller.AccountController;
import bw.edu.banking.controller.LoginController;
import bw.edu.banking.service.BankingService;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.YearMonth;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

/** Real end-to-end service -> model -> DAO -> SQLite checks, without a display. */
public class BankingIntegrationTest {
    @TempDir Path temporary;
    private BankingService service;
    private Customer customer;

    @BeforeEach void setUp() {
        service = new BankingService(temporary.resolve("test.db"));
        customer = service.register("Poko", "Gosekwang", "Gaborone", "poko@example.test",
                "StrongPass!1", "Engineering Co", "Gaborone CBD");
    }

    @Test void registeringAndLoggingInUsesPersistedCredentials() {
        BankingService reopened = new BankingService(temporary.resolve("test.db"));
        assertEquals(customer.getId(), reopened.login("POKO@example.test", "StrongPass!1").getId());
        assertThrows(IllegalArgumentException.class, () -> reopened.login("poko@example.test", "wrong"));
        assertFalse(customer.getPasswordHash().contains("StrongPass!1"));
    }

    @Test void savingsAllowsDepositsButNeverWithdrawals() {
        var savings = service.openAccount(customer.getId(), AccountType.SAVINGS, new BigDecimal("100.00"), "Gaborone");
        assertEquals(new BigDecimal("150.00"), service.deposit(customer.getId(), savings.getAccountNumber(),
                new BigDecimal("50.00"), "Cash"));
        assertThrows(UnsupportedOperationException.class, () -> service.withdraw(customer.getId(), savings.getAccountNumber(),
                new BigDecimal("1.00")));
        assertEquals(2, service.history(customer.getId(), savings.getAccountNumber()).size());
        assertEquals(new BigDecimal("150.00"), service.listAccounts(customer.getId()).get(0).getBalance());
    }

    @Test void investmentEnforcesOpeningMinimumButAllowsLaterWithdrawals() {
        assertThrows(IllegalArgumentException.class,
                () -> service.openAccount(customer.getId(), AccountType.INVESTMENT, new BigDecimal("499.99"), "Gaborone"));
        var investment = service.openAccount(customer.getId(), AccountType.INVESTMENT, new BigDecimal("500.00"), "Gaborone");
        assertEquals(new BigDecimal("100.00"), service.withdraw(customer.getId(), investment.getAccountNumber(),
                new BigDecimal("400.00")));
        assertThrows(IllegalArgumentException.class,
                () -> service.withdraw(customer.getId(), investment.getAccountNumber(), new BigDecimal("101.00")));
    }

    @Test void chequeAccountRequiresEmploymentDetails() {
        var unemployed = service.register("Neo", "Moyo", "Mochudi", "neo@example.test", "StrongPass!1", "", "");
        assertThrows(IllegalArgumentException.class,
                () -> service.openAccount(unemployed.getId(), AccountType.CHEQUE, BigDecimal.ZERO, "Gaborone"));
        var cheque = service.openAccount(customer.getId(), AccountType.CHEQUE, BigDecimal.ZERO, "Gaborone");
        assertEquals(new BigDecimal("150.00"), service.deposit(customer.getId(), cheque.getAccountNumber(),
                new BigDecimal("150.00"), "Salary"));
        assertEquals(new BigDecimal("50.00"), service.withdraw(customer.getId(), cheque.getAccountNumber(),
                new BigDecimal("100.00")));
    }

    @Test void interestRatesAreExactAndPaidOnlyOncePerMonth() {
        var savings = service.openAccount(customer.getId(), AccountType.SAVINGS, new BigDecimal("500.00"), "Main");
        var investment = service.openAccount(customer.getId(), AccountType.INVESTMENT, new BigDecimal("500.00"), "Main");
        YearMonth month = YearMonth.of(2026, 11);
        assertEquals(new BigDecimal("0.25"), service.payMonthlyInterest(customer.getId(), savings.getAccountNumber(), month));
        assertEquals(new BigDecimal("25.00"), service.payMonthlyInterest(customer.getId(), investment.getAccountNumber(), month));
        assertThrows(IllegalArgumentException.class,
                () -> service.payMonthlyInterest(customer.getId(), investment.getAccountNumber(), month));
        assertEquals(new BigDecimal("525.00"), service.listAccounts(customer.getId()).stream()
                .filter(a -> a.getAccountNumber().equals(investment.getAccountNumber())).findFirst().orElseThrow().getBalance());
    }

    @Test void moneyIsValidatedAndOtherCustomersCannotAccessAccount() {
        var account = service.openAccount(customer.getId(), AccountType.SAVINGS, BigDecimal.ZERO, "Main");
        var stranger = service.register("Dineo", "Dube", "Maun", "dineo@example.test", "StrongPass!1", "", "");
        assertThrows(IllegalArgumentException.class,
                () -> service.deposit(stranger.getId(), account.getAccountNumber(), BigDecimal.TEN, "Unauthorised"));
        assertThrows(IllegalArgumentException.class,
                () -> service.deposit(customer.getId(), account.getAccountNumber(), new BigDecimal("1.234"), "Wrong decimals"));
        assertEquals(new BigDecimal("0.00"), service.listAccounts(customer.getId()).get(0).getBalance());
        assertEquals(1, service.history(customer.getId(), account.getAccountNumber()).size());
    }

    @Test void controllersConnectAuthenticationAccountsAndPersistedHistory() {
        LoginController login = new LoginController(service);
        Customer signedIn = login.login("poko@example.test", "StrongPass!1");
        AccountController accountController = new AccountController(service, signedIn.getId());
        accountController.openAccount(AccountType.SAVINGS, "100.00", "Gaborone Main");
        String number = accountController.accounts().get(0).number();
        accountController.deposit(number, "20.00", "Controller integration test");
        assertEquals(new BigDecimal("120.00"), accountController.accounts().get(0).balance());
        assertEquals(2, accountController.history(number).size());
        BankingService reopened = new BankingService(temporary.resolve("test.db"));
        assertEquals(new BigDecimal("120.00"), reopened.listAccounts(customer.getId()).get(0).getBalance());
    }
}
