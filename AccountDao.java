package bw.edu.banking.data;

import bw.edu.banking.model.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class AccountDao {
    public void insert(Connection connection, Account account) throws SQLException {
        String sql = "INSERT INTO accounts (account_number,customer_id,account_type,branch,date_opened,balance_cents) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement query = connection.prepareStatement(sql)) {
            query.setString(1, account.getAccountNumber());
            query.setString(2, account.getCustomerId());
            query.setString(3, account.getType().name());
            query.setString(4, account.getBranch());
            query.setString(5, account.getDateOpened().toString());
            query.setLong(6, Money.toCents(account.getBalance()));
            query.executeUpdate();
        }
    }

    public Optional<Account> findByNumber(Connection connection, String number) throws SQLException {
        try (PreparedStatement query = connection.prepareStatement("SELECT * FROM accounts WHERE account_number = ?")) {
            query.setString(1, number);
            try (ResultSet results = query.executeQuery()) {
                return results.next() ? Optional.of(map(results)) : Optional.empty();
            }
        }
    }

    public List<Account> findByCustomer(Connection connection, String customerId) throws SQLException {
        List<Account> accounts = new ArrayList<>();
        try (PreparedStatement query = connection.prepareStatement("SELECT * FROM accounts WHERE customer_id = ? ORDER BY date_opened, account_number")) {
            query.setString(1, customerId);
            try (ResultSet results = query.executeQuery()) {
                while (results.next()) accounts.add(map(results));
            }
        }
        return accounts;
    }

    public void update(Connection connection, Account account) throws SQLException {
        try (PreparedStatement query = connection.prepareStatement("UPDATE accounts SET balance_cents = ? WHERE account_number = ?")) {
            query.setLong(1, Money.toCents(account.getBalance()));
            query.setString(2, account.getAccountNumber());
            if (query.executeUpdate() != 1) throw new SQLException("Account no longer exists");
        }
    }

    public void delete(Connection connection, String number) throws SQLException {
        try (PreparedStatement query = connection.prepareStatement("DELETE FROM accounts WHERE account_number = ?")) {
            query.setString(1, number);
            query.executeUpdate();
        }
    }

    private Account map(ResultSet row) throws SQLException {
        String number = row.getString("account_number");
        String customer = row.getString("customer_id");
        String branch = row.getString("branch");
        LocalDate date = LocalDate.parse(row.getString("date_opened"));
        var amount = Money.fromCents(row.getLong("balance_cents"));
        return switch (AccountType.valueOf(row.getString("account_type"))) {
            case SAVINGS -> new SavingsAccount(number, customer, branch, date, amount);
            case INVESTMENT -> new InvestmentAccount(number, customer, branch, date, amount);
            case CHEQUE -> new ChequeAccount(number, customer, branch, date, amount);
        };
    }
}
