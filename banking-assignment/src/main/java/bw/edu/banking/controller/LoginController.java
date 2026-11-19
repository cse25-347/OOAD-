package bw.edu.banking.controller;

import bw.edu.banking.model.Customer;
import bw.edu.banking.service.BankingService;

/** GUI boundary calls controller; controller calls application service. */
public final class LoginController {
    private final BankingService service;

    public LoginController(BankingService service) { this.service = service; }
    public Customer login(String email, String password) { return service.login(email, password); }
    public Customer register(String first, String last, String address, String email,
                             String password, String employer, String employerAddress) {
        return service.register(first, last, address, email, password, employer, employerAddress);
    }
}
