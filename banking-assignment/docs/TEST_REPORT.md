# Banking System Integration Test Report

Date: 27 October 2026

## Test method

Seven automated JUnit tests exercise the application services, controller interfaces, OOAD model, JDBC DAOs, and temporary SQLite databases. Each test starts with a separate database, avoiding shared-state pollution. A separate sample-data run confirmed that ten demonstration customers were inserted.

The Java source compiled and the seven tests **passed** in this environment using Java 17, OpenJFX 11, SQLite JDBC 3.44.1 and JUnit 5.10.1 supplied as system packages. The submitted Maven `pom.xml` targets newer artifact versions from Maven Central; that exact online Maven build was not verified here because Maven Central DNS resolution failed. Retest with `mvn clean test` in the intended GitHub Codespace.

| Test case | Expected and actual behaviour | Result |
| --- | --- | --- |
| Registration and login | Persisted user signs in; wrong password fails; plaintext password is not saved | PASS |
| Savings account | Deposits increase balance, withdrawals are rejected, history survives | PASS |
| Investment account | Opening below P500 fails; P500 succeeds; withdrawals allowed afterwards | PASS |
| Cheque account | Employer information required; employed user can deposit/withdraw | PASS |
| Monthly interest | Savings rate is 0.05%, investment is 5%, same month cannot be paid twice | PASS |
| Owner isolation and amount validation | Stranger cannot deposit to another user's account; 3-decimal input rejected | PASS |
| Controller and persistence flow | LoginController and AccountController operate through DB; balance persists after reopen | PASS |

## Manual GUI test still required

JavaFX screen classes compiled, but no desktop GUI was launched in the headless verification environment. On a graphical machine, confirm registration, login, account selection, opening, deposit, withdrawal error alerts, interest posting, refresh and logout. Add genuine screenshots and the lecturer's GitHub repository link to your personal submission.

## Synthetic sample records

`SeedData` creates ten fictional customer records, one or more account rows per customer and corresponding transaction history. Run:

```bash
mvn exec:java -Dexec.mainClass=bw.edu.banking.SeedData
```

Example demo login: `demo1@example.test` / `Demo@12345`. Never use this fixed password outside the educational demonstration.
