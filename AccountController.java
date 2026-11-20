package bw.edu.banking.controller;

import bw.edu.banking.model.Account;
import bw.edu.banking.model.AccountType;
import bw.edu.banking.model.BankTransaction;
import bw.edu.banking.service.BankingService;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

public final class AccountController {
    private final BankingService service;
    private final String customerId;

    public AccountController(BankingService service, String customerId) {
        this.service = service;
        this.customerId = customerId;
    }

    public List<AccountSummary> accounts() {
        return service.listAccounts(customerId).stream()
                .map(a -> new AccountSummary(a.getAccountNumber(), a.getType(), a.getBalance(), a.getBranch()))
                .toList();
    }

    public void openAccount(AccountType type, String initial, String branch) {
        service.openAccount(customerId, type, parseAmount(initial), branch);
    }

    public void deposit(String number, String amount, String reference) {
        service.deposit(customerId, number, parseAmount(amount), reference);
    }

    public void withdraw(String number, String amount) {
        service.withdraw(customerId, number, parseAmount(amount));
    }

    public BigDecimal payInterest(String number) {
        return service.payMonthlyInterest(customerId, number, YearMonth.now());
    }

    public List<BankTransaction> history(String number) { return service.history(customerId, number); }

    private BigDecimal parseAmount(String input) {
        try { return new BigDecimal(input.trim()); }
        catch (RuntimeException invalid) { throw new IllegalArgumentException("Enter a valid amount, for example 500.00"); }
    }

    public record AccountSummary(String number, AccountType type, BigDecimal balance, String branch) {
        @Override public String toString() { return type + " | " + number + " | BWP " + balance; }
    }
}
