package bw.edu.banking;

import bw.edu.banking.model.AccountType;
import bw.edu.banking.service.BankingService;
import java.math.BigDecimal;
import java.nio.file.Path;

/** Optional 10-record dataset for the lecturer's presentation requirement. */
public final class SeedData {
    private SeedData() { }

    public static void main(String[] args) {
        BankingService bank = new BankingService(Path.of(System.getProperty("bank.db", "banking.db")));
        String[][] people = {
                {"Amantle", "Molefe"}, {"Kabelo", "Kgosi"}, {"Dineo", "Phiri"},
                {"Thato", "Moagi"}, {"Neo", "Dube"}, {"Lorato", "Modise"},
                {"Onalenna", "Tau"}, {"Boitumelo", "Moyo"}, {"Tebogo", "Kgosidintsi"},
                {"Refilwe", "Motsumi"}
        };
        int inserted = 0;
        for (int i = 0; i < people.length; i++) {
            String email = "demo" + (i + 1) + "@example.test";
            try {
                String employer = i % 2 == 0 ? "Demo Employer " + (i + 1) : "";
                String companyAddress = employer.isBlank() ? "" : "CBD, Gaborone";
                var customer = bank.register(people[i][0], people[i][1], "Gaborone, Botswana", email,
                        "Demo@12345", employer, companyAddress);
                AccountType type = i % 3 == 0 ? AccountType.INVESTMENT
                        : i % 3 == 1 ? AccountType.SAVINGS : AccountType.CHEQUE;
                if (type == AccountType.CHEQUE && employer.isBlank()) type = AccountType.SAVINGS;
                var account = bank.openAccount(customer.getId(), type, new BigDecimal("600.00"), "Gaborone Main");
                bank.deposit(customer.getId(), account.getAccountNumber(), new BigDecimal("50.00"), "Demo salary or cash");
                inserted++;
            } catch (IllegalArgumentException alreadyCreated) {
                if (!alreadyCreated.getMessage().equals("Email already registered")) throw alreadyCreated;
            }
        }
        System.out.println("Created " + inserted + " demo customers (10 total after first run).");
        System.out.println("Example sign-in: demo1@example.test / Demo@12345");
    }
}
