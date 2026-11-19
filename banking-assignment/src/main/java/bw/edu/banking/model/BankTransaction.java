package bw.edu.banking.model;

import java.math.BigDecimal;
import java.time.Instant;

public record BankTransaction(long id, String accountNumber, String kind, BigDecimal amount,
                              BigDecimal balanceAfter, String reference, Instant occurredAt) { }
