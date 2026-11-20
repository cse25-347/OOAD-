# OOAD Banking System | JavaFX and JDBC

This is an educational implementation for the CSE202 Banking System scenario. It contains Part B's core model, JavaFX boundary views, controllers, SQLite DAOs, SQL schema, sample data, and integration tests. Review and adapt the example yourself before presenting it as individual coursework.

## Requirements

- Java Development Kit 17 or later
- Maven 3.8+
- Graphical desktop or a Codespace with desktop forwarding for JavaFX

JavaFX 21, SQLite JDBC, and JUnit dependencies are downloaded automatically through Maven. The GUI requires a desktop display; headless Codespaces can still compile and run tests.

## Commands

From the `banking-assignment` directory:

```bash
mvn clean test
mvn javafx:run
```

The application creates `banking.db` in the current working directory. To create the lecturer's ten demonstration customer records, run once:

```bash
mvn exec:java -Dexec.mainClass=bw.edu.banking.SeedData
```

This populates ten customers, ten accounts and example transactions. Re-running does not duplicate existing demo users. Example login: `demo1@example.test` with `Demo@12345`. These are **fictional demonstration credentials** and must never be used in a real banking system.

For a different database path, add a JVM system property `-Dbank.db=/path/to/file.db` when running Java. Avoid committing database files or passwords to Git.

## Structure

| Package | Responsibility |
| --- | --- |
| `model` | Abstract `Account`, subclasses, `Customer`, `InterestBearing` interface, money rules |
| `view` | `LoginView`, `AccountView` JavaFX input and display only |
| `controller` | Translates GUI commands into use-case operations |
| `service` | Registration, authentication, opening, payments, interest, ownership checks, DB transactions |
| `data` | JDBC schema initialization and Customer, Account and Transaction DAO operations |
| `test` | Integrated use-case tests with isolated SQLite databases |

## Rules deliberately enforced

1. One customer can own multiple account rows. Every account has a foreign key to an existing customer.
2. Savings permits deposits but rejects all withdrawals.
3. Investment needs at least BWP 500 **when opened**; later withdrawals can reduce the balance.
4. Cheque needs employer and employer address from registration; it pays no interest.
5. Monthly interest is 5% (investment) and 0.05% (savings), rounded to thebe using HALF_UP, once per account/month.
6. All money in SQLite is held as integer thebe to avoid binary floating-point money errors.
7. Each successful balance change and its transaction-history entry commit together (or both roll back).
8. Passwords are salted PBKDF2 hashes, not plaintext. This is still a classroom system, not production banking software.

## GitHub Codespaces handoff

1. Create your own GitHub repository, upload the **project files**, and make commits at each weekly stage.
2. Open the repository in Codespaces. Run `mvn clean test` in its terminal.
3. To open JavaFX GUI, use a local graphical computer or configure graphical forwarding in Codespaces. JavaFX normally cannot display on a terminal-only Codespace.
4. Run the seed command before presentation, and demonstrate a savings deposit, blocked savings withdrawal, investment interest, employment check and persisted history.

## Important scope and limitations

This is a classroom prototype. The employer details are self-reported rather than verified by a bank. No external payment gateways, KYC identity checks, role-based teller administration, or real payroll integration are included. The application has no automatic background monthly scheduler: the **Pay this month's interest** button runs the idempotent monthly operation for the chosen account. A genuine bank would use audit-controlled schedules and financial regulations.

See `docs/uml` for editable diagram source, and the separate walkthrough notes for explanations and evidence of test cases. The live GitHub repository, actual client interview, student identification details and screenshots of a running GUI must be supplied by you.
