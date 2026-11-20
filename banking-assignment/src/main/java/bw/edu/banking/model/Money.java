package bw.edu.banking.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Money {
    private Money() { }

    public static BigDecimal normalise(BigDecimal value) {
        if (value == null) throw new IllegalArgumentException("Amount is required");
        try {
            return value.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException error) {
            throw new IllegalArgumentException("Use at most two decimal places", error);
        }
    }

    public static BigDecimal positive(BigDecimal value) {
        BigDecimal result = normalise(value);
        if (result.signum() <= 0) throw new IllegalArgumentException("Amount must be greater than zero");
        return result;
    }

    public static long toCents(BigDecimal amount) {
        return normalise(amount).movePointRight(2).longValueExact();
    }

    public static BigDecimal fromCents(long cents) {
        return BigDecimal.valueOf(cents, 2);
    }
}
