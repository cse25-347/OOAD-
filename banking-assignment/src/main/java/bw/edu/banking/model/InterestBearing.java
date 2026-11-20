package bw.edu.banking.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Interface (contract) for the account types entitled to monthly interest. */
public interface InterestBearing {
    BigDecimal monthlyRate();

    default BigDecimal interestOn(BigDecimal balance) {
        return balance.multiply(monthlyRate()).setScale(2, RoundingMode.HALF_UP);
    }
}
