package bw.edu.banking.service;

import bw.edu.banking.data.*;
import bw.edu.banking.model.*;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Application use cases; transaction boundaries keep balance and audit log in sync. */
public final class BankingService {
    private final DatabaseManager db;
    private final CustomerDao customers = new CustomerDao();
    private final AccountDao accounts = new AccountDao();
    private final TransactionDao transactions = new TransactionDao();

    public BankingService(Path file) {
        db = new DatabaseManager(file);
        db.initialise();
    }

    @FunctionalInterface private interface SqlAction<T> { T run(Connection connection) throws SQLException; }

    private <T> T execute(SqlAction<T> action) {
        try (Connection connection = db.connect()) {
            connection.setAutoCommit(false);
            try {
                T result = action.run(connection);
                connection.commit();
                return result;
            } catch (SQLException | RuntimeException failure) {
                try { connection.rollback(); } catch (SQLException rollback) { failure.addSuppressed(rollback); }
                throw failure;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Database operation failed: " + exception.getMessage(), exception);
        }
    }

    public Customer register(String firstName, String surname, String address, String email,
                             String password, String employer, String employerAddress) {
        String f = required(firstName, "First name");
        String s = required(surname, "Surname");
        String a = required(address, "Address");
        String e = required(email, "Email").toLowerCase(Locale.ROOT);
        if (!e.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) throw new IllegalArgumentException("Enter a valid email");
        if (password == null || password.length() < 8) throw new IllegalArgumentException("Password needs at least 8 characters");
        return execute(connection -> {
            if (customers.findByEmail(connection, e).isPresent()) throw new IllegalArgumentException("Email already registered");
            Customer customer = new Customer(UUID.randomUUID().toString(), f, s, a, e,
                    PasswordHasher.hash(password.toCharArray()), trimmed(employer), trimmed(employerAddress));
            customers.insert(connection, customer);
            return customer;
        });
    }

    public Customer login(String email, String password) {
        if (email == null || password == null) throw new IllegalArgumentException("Email and password required");
        return execute(connection -> {
            Customer customer = customers.findByEmail(connection, email.trim().toLowerCase(Locale.ROOT))
                    .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));
            if (!PasswordHasher.verify(password.toCharArray(), customer.getPasswordHash()))
                throw new IllegalArgumentException("Invalid email or password");
            return customer;
        });
    }

    public Account openAccount(String customerId, AccountType type, BigDecimal initialDeposit, String branch) {
        if (type == null) throw new IllegalArgumentException("Select an account type");
        BigDecimal initial = Money.normalise(initialDeposit);
        if (initial.signum() < 0) throw new IllegalArgumentException("Initial deposit cannot be negative");
        if (type == AccountType.INVESTMENT && initial.compareTo(InvestmentAccount.OPENING_MINIMUM) < 0)
            throw new IllegalArgumentException("Investment accounts need at least BWP 500 on opening");
        String selectedBranch = required(branch, "Branch");
        return execute(connection -> {
            Customer owner = customers.findById(connection, customerId)
                    .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
            if (type == AccountType.CHEQUE && !owner.isEmployed())
                throw new IllegalArgumentException("A cheque account requires employer name and address");
            String number = "BW" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase(Locale.ROOT);
            Account account = switch (type) {
                case SAVINGS -> new SavingsAccount(number, customerId, selectedBranch, LocalDate.now(), initial);
                case INVESTMENT -> new InvestmentAccount(number, customerId, selectedBranch, LocalDate.now(), initial);
                case CHEQUE -> new ChequeAccount(number, customerId, selectedBranch, LocalDate.now(), initial);
            };
            accounts.insert(connection, account);
            transactions.insert(connection, number, "OPENING", initial, account.getBalance(), "Opening deposit");
            return account;
        });
    }

    public List<Account> listAccounts(String customerId) {
        return execute(connection -> accounts.findByCustomer(connection, customerId));
    }

    private Account ownedAccount(Connection connection, String customerId, String number) throws SQLException {
        Account account = accounts.findByNumber(connection, number)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        if (!account.getCustomerId().equals(customerId)) throw new IllegalArgumentException("Account does not belong to this customer");
        return account;
    }

    public BigDecimal deposit(String customerId, String number, BigDecimal amount, String reference) {
        BigDecimal money = Money.positive(amount);
        String description = required(reference, "Reference");
        return execute(connection -> {
            Account account = ownedAccount(connection, customerId, number);
            account.deposit(money, description);
            accounts.update(connection, account);
            transactions.insert(connection, number, "DEPOSIT", money, account.getBalance(), description);
            return account.getBalance();
        });
    }

    public BigDecimal withdraw(String customerId, String number, BigDecimal amount) {
        BigDecimal money = Money.positive(amount);
        return execute(connection -> {
            Account account = ownedAccount(connection, customerId, number);
            account.withdraw(money); // subclass decides whether allowed
            accounts.update(connection, account);
            transactions.insert(connection, number, "WITHDRAWAL", money, account.getBalance(), "Withdrawal");
            return account.getBalance();
        });
    }

    public BigDecimal payMonthlyInterest(String customerId, String number, YearMonth month) {
        if (month == null) throw new IllegalArgumentException("Month is required");
        return execute(connection -> {
            Account account = ownedAccount(connection, customerId, number);
            if (!(account instanceof InterestBearing interest))
                throw new IllegalArgumentException("Cheque accounts do not earn interest");
            if (!transactions.claimInterestMonth(connection, number, month.toString()))
                throw new IllegalArgumentException("Interest already paid for " + month);
            BigDecimal earned = interest.interestOn(account.getBalance());
            if (earned.signum() > 0) account.deposit(earned, "Monthly interest");
            accounts.update(connection, account);
            transactions.insert(connection, number, "INTEREST", earned, account.getBalance(), "Monthly interest " + month);
            return earned;
        });
    }

    public List<BankTransaction> history(String customerId, String number) {
        return execute(connection -> {
            ownedAccount(connection, customerId, number);
            return transactions.findByAccount(connection, number);
        });
    }

    private static String required(String value, String field) {
        String result = trimmed(value);
        if (result.isBlank()) throw new IllegalArgumentException(field + " is required");
        return result;
    }

    private static String trimmed(String value) { return value == null ? "" : value.trim(); }
}
