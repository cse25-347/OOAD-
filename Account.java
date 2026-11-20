package bw.edu.banking.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/** Abstract superclass: common account data and deposit behaviour. */
public abstract class Account {
    private final String accountNumber;
    private final String customerId;
    private final String branch;
    private final LocalDate dateOpened;
    private BigDecimal balance;

    protected Account(String accountNumber, String customerId, String branch,
                      LocalDate dateOpened, BigDecimal balance) {
        this.accountNumber = Objects.requireNonNull(accountNumber);
        this.customerId = Objects.requireNonNull(customerId);
        if (customerId.isBlank()) throw new IllegalArgumentException("Account needs a customer");
        this.branch = Objects.requireNonNull(branch);
        this.dateOpened = Objects.requireNonNull(dateOpened);
        this.balance = Money.normalise(balance);
        if (this.balance.signum() < 0) throw new IllegalArgumentException("Negative opening balance");
    }

    public String getAccountNumber() { return accountNumber; }
    public String getCustomerId() { return customerId; }
    public String getBranch() { return branch; }
    public LocalDate getDateOpened() { return dateOpened; }
    public BigDecimal getBalance() { return balance; }

    // Method overloading: both deposit signatures are valid.
    public final void deposit(BigDecimal amount) { deposit(amount, "Deposit"); }

    public final void deposit(BigDecimal amount, String reference) {
        Money.positive(amount);
        if (reference == null || reference.isBlank()) throw new IllegalArgumentException("Reference required");
        balance = balance.add(amount);
    }

    protected final void debit(BigDecimal amount) {
        Money.positive(amount);
        if (amount.compareTo(balance) > 0) throw new IllegalArgumentException("Insufficient funds");
        balance = balance.subtract(amount);
    }

    public abstract AccountType getType();

    // Polymorphism: behaviour depends on actual subclass.
    public abstract void withdraw(BigDecimal amount);
}
