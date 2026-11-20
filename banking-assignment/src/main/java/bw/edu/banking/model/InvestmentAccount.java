package bw.edu.banking.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class InvestmentAccount extends Account implements InterestBearing {
    public static final BigDecimal OPENING_MINIMUM = new BigDecimal("500.00");

    public InvestmentAccount(String id, String customerId, String branch, LocalDate opened, BigDecimal balance) {
        super(id, customerId, branch, opened, balance);
    }

    @Override public AccountType getType() { return AccountType.INVESTMENT; }
    @Override public BigDecimal monthlyRate() { return new BigDecimal("0.05"); } // 5%
    @Override public void withdraw(BigDecimal amount) { debit(amount); }
}
