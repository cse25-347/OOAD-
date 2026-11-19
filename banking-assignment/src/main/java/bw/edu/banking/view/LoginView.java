package bw.edu.banking.view;

import bw.edu.banking.controller.LoginController;
import bw.edu.banking.model.Customer;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/** Boundary class: collects and displays inputs, with no banking or SQL logic. */
public final class LoginView extends BorderPane {
    private final LoginController controller;
    private final Consumer<Customer> onSuccess;

    public LoginView(LoginController controller, Consumer<Customer> onSuccess) {
        this.controller = controller;
        this.onSuccess = onSuccess;
        setStyle("-fx-background-color: #f2f5f9;");
        Label heading = new Label("BANKING SYSTEM");
        heading.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: #183153;");
        Label subheading = new Label("Object Oriented Analysis and Design with Java");
        subheading.setStyle("-fx-font-size: 14px; -fx-text-fill: #5c6b7d;");
        VBox top = new VBox(8, heading, subheading);
        top.setAlignment(Pos.CENTER);
        top.setPadding(new Insets(35, 20, 18, 20));
        setTop(top);

        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getTabs().add(new Tab("Sign in", loginForm()));
        tabs.getTabs().add(new Tab("Register", registrationForm()));
        tabs.setMaxWidth(610);
        setCenter(new StackPane(tabs));
        BorderPane.setMargin(tabs, new Insets(0, 180, 40, 180));
    }

    private VBox loginForm() {
        TextField email = new TextField(); email.setPromptText("Email address");
        PasswordField password = new PasswordField(); password.setPromptText("Password");
        Button submit = new Button("Sign in");
        submit.setMaxWidth(Double.MAX_VALUE);
        submit.setStyle("-fx-background-color: #205b97; -fx-text-fill: white; -fx-font-weight: bold;");
        submit.setOnAction(event -> act(() -> onSuccess.accept(controller.login(email.getText(), password.getText()))));
        VBox content = new VBox(14, new Label("Email"), email, new Label("Password"), password, submit,
                new Label("Need a profile? Open the Register tab."));
        content.setPadding(new Insets(30));
        return content;
    }

    private VBox registrationForm() {
        TextField first = field("First name"); TextField last = field("Surname");
        TextField address = field("Residential address"); TextField email = field("Email");
        PasswordField password = new PasswordField(); password.setPromptText("Password (8+ characters)");
        TextField employer = field("Employer name (optional)");
        TextField employerAddress = field("Employer address (optional)");
        Label hint = new Label("Both employer fields are required to open a cheque account later.");
        hint.setWrapText(true);
        Button submit = new Button("Create customer profile");
        submit.setStyle("-fx-background-color: #205b97; -fx-text-fill: white;");
        submit.setMaxWidth(Double.MAX_VALUE);
        submit.setOnAction(event -> act(() -> onSuccess.accept(controller.register(
                first.getText(), last.getText(), address.getText(), email.getText(), password.getText(),
                employer.getText(), employerAddress.getText()))));
        VBox content = new VBox(10, first, last, address, email, password, employer, employerAddress, hint, submit);
        content.setPadding(new Insets(24));
        ScrollPane scroll = new ScrollPane(content); scroll.setFitToWidth(true);
        VBox holder = new VBox(scroll); VBox.setVgrow(scroll, Priority.ALWAYS);
        return holder;
    }

    private TextField field(String prompt) { TextField field = new TextField(); field.setPromptText(prompt); return field; }

    private void act(Runnable action) {
        try { action.run(); }
        catch (RuntimeException error) { new Alert(Alert.AlertType.ERROR, error.getMessage(), ButtonType.OK).showAndWait(); }
    }
}
