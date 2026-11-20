package bw.edu.banking;

import bw.edu.banking.controller.AccountController;
import bw.edu.banking.controller.LoginController;
import bw.edu.banking.model.Customer;
import bw.edu.banking.service.BankingService;
import bw.edu.banking.view.AccountView;
import bw.edu.banking.view.LoginView;
import java.nio.file.Path;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/** Starts the desktop JavaFX banking demonstration. */
public final class BankingApp extends Application {
    private BankingService service;
    private Stage window;

    @Override public void start(Stage stage) {
        window = stage;
        service = new BankingService(Path.of(System.getProperty("bank.db", "banking.db")));
        window.setTitle("Banking System | OOAD Java Assignment");
        showLogin();
        window.show();
    }

    private void showLogin() {
        LoginView view = new LoginView(new LoginController(service), this::showAccounts);
        window.setScene(new Scene(view, 970, 660));
    }

    private void showAccounts(Customer customer) {
        AccountView view = new AccountView(new AccountController(service, customer.getId()),
                customer.getFullName(), this::showLogin);
        window.setScene(new Scene(view, 1050, 700));
    }

    public static void main(String[] args) { launch(args); }
}
