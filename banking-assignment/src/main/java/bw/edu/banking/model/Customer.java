package bw.edu.banking.model;

import java.util.Objects;

public final class Customer {
    private final String id;
    private final String firstName;
    private final String surname;
    private final String address;
    private final String email;
    private final String passwordHash;
    private final String employer;
    private final String employerAddress;

    public Customer(String id, String firstName, String surname, String address, String email,
                    String passwordHash, String employer, String employerAddress) {
        this.id = Objects.requireNonNull(id);
        this.firstName = Objects.requireNonNull(firstName);
        this.surname = Objects.requireNonNull(surname);
        this.address = Objects.requireNonNull(address);
        this.email = Objects.requireNonNull(email);
        this.passwordHash = Objects.requireNonNull(passwordHash);
        this.employer = employer == null ? "" : employer;
        this.employerAddress = employerAddress == null ? "" : employerAddress;
    }

    public String getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getSurname() { return surname; }
    public String getAddress() { return address; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getEmployer() { return employer; }
    public String getEmployerAddress() { return employerAddress; }
    public String getFullName() { return firstName + " " + surname; }
    public boolean isEmployed() { return !employer.isBlank() && !employerAddress.isBlank(); }
}
