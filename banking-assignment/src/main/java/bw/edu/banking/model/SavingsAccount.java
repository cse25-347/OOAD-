package bw.edu.banking.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class SavingsAccount extends Account implements InterestBearing {
    public SavingsAccount(String id, String customerId, String branch, LocalDate opened, BigDecimal balance) {
        super(id, customerId, branch, opened, balance);
    }

    @Override public AccountType getType() { return AccountType.SAVINGS; }
    @Override public BigDecimal monthlyRate() { return new BigDecimal("0.0005"); } // 0.05%

    @Override public void withdraw(BigDecimal amount) {
        throw new UnsupportedOperationException("Savings accounts do not allow withdrawals");
    }
}
