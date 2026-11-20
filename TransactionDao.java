package bw.edu.banking.data;

import bw.edu.banking.model.BankTransaction;
import bw.edu.banking.model.Money;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class TransactionDao {
    public void insert(Connection connection, String accountNumber, String type, BigDecimal amount,
                       BigDecimal balanceAfter, String reference) throws SQLException {
        String sql = "INSERT INTO transactions (account_number,kind,amount_cents,balance_after_cents,reference,occurred_at) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement query = connection.prepareStatement(sql)) {
            query.setString(1, accountNumber);
            query.setString(2, type);
            query.setLong(3, Money.toCents(amount));
            query.setLong(4, Money.toCents(balanceAfter));
            query.setString(5, reference);
            query.setString(6, Instant.now().toString());
            query.executeUpdate();
        }
    }

    public List<BankTransaction> findByAccount(Connection connection, String number) throws SQLException {
        List<BankTransaction> history = new ArrayList<>();
        try (PreparedStatement query = connection.prepareStatement("SELECT * FROM transactions WHERE account_number = ? ORDER BY id DESC")) {
            query.setString(1, number);
            try (ResultSet rows = query.executeQuery()) {
                while (rows.next()) history.add(new BankTransaction(
                        rows.getLong("id"), rows.getString("account_number"), rows.getString("kind"),
                        Money.fromCents(rows.getLong("amount_cents")),
                        Money.fromCents(rows.getLong("balance_after_cents")),
                        rows.getString("reference"), Instant.parse(rows.getString("occurred_at"))));
            }
        }
        return history;
    }

    /** Unique month marker prevents double interest, including repeated button clicks. */
    public boolean claimInterestMonth(Connection connection, String accountNumber, String month) throws SQLException {
        String sql = "INSERT OR IGNORE INTO interest_postings (account_number,month_key) VALUES (?,?)";
        try (PreparedStatement query = connection.prepareStatement(sql)) {
            query.setString(1, accountNumber);
            query.setString(2, month);
            return query.executeUpdate() == 1;
        }
    }

    public void deleteForAccount(Connection connection, String number) throws SQLException {
        try (PreparedStatement query = connection.prepareStatement("DELETE FROM transactions WHERE account_number = ?")) {
            query.setString(1, number);
            query.executeUpdate();
        }
    }
}
