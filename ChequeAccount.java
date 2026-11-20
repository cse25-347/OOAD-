package bw.edu.banking.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class ChequeAccount extends Account {
    public ChequeAccount(String id, String customerId, String branch, LocalDate opened, BigDecimal balance) {
        super(id, customerId, branch, opened, balance);
    }

    @Override public AccountType getType() { return AccountType.CHEQUE; }
    @Override public void withdraw(BigDecimal amount) { debit(amount); }
}
