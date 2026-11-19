package bw.edu.banking.view;

import bw.edu.banking.controller.AccountController;
import bw.edu.banking.controller.AccountController.AccountSummary;
import bw.edu.banking.model.AccountType;
import bw.edu.banking.model.BankTransaction;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/** JavaFX boundary, deliberately free of domain calculation and database queries. */
public final class AccountView extends BorderPane {
    private final AccountController controller;
    private final ComboBox<AccountSummary> accounts = new ComboBox<>();
    private final TextArea history = new TextArea();
    private final Label status = new Label();

    public AccountView(AccountController controller, String customerName, Runnable onLogout) {
        this.controller = controller;
        setStyle("-fx-background-color: #f4f7fa;");
        Label title = new Label("Welcome, " + customerName);
        title.setStyle("-fx-font-size: 23px; -fx-font-weight: bold; -fx-text-fill: #183153;");
        Button logout = new Button("Log out"); logout.setOnAction(event -> onLogout.run());
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox header = new HBox(15, title, spacer, logout);
        header.setAlignment(Pos.CENTER_LEFT); header.setPadding(new Insets(22));
        setTop(header);

        VBox tools = new VBox(18, new Label("OPEN AN ACCOUNT"), openForm(), new Separator(),
                new Label("MANAGE AN ACCOUNT"), actionForm());
        tools.setPadding(new Insets(24)); tools.setPrefWidth(410);
        tools.setStyle("-fx-background-color: white;");
        setLeft(tools);

        accounts.setMaxWidth(Double.MAX_VALUE);
        accounts.setOnAction(event -> refreshHistory());
        history.setEditable(false); history.setWrapText(false);
        history.setStyle("-fx-font-family: monospace; -fx-font-size: 12px;");
        Button refresh = new Button("Refresh accounts and history");
        refresh.setOnAction(event -> refresh());
        Label description = new Label("TRANSACTION HISTORY");
        description.setStyle("-fx-font-weight: bold;");
        VBox data = new VBox(12, new Label("Your accounts"), accounts, refresh, description, history, status);
        VBox.setVgrow(history, Priority.ALWAYS);
        data.setPadding(new Insets(24));
        setCenter(data);
        refresh();
    }

    private VBox openForm() {
        ComboBox<AccountType> type = new ComboBox<>(FXCollections.observableArrayList(AccountType.values()));
        type.setValue(AccountType.SAVINGS); type.setMaxWidth(Double.MAX_VALUE);
        TextField opening = new TextField("0.00");
        TextField branch = new TextField("Gaborone Main");
        Label note = new Label("Investment: minimum P500 opening. Cheque: employed customers only.");
        note.setWrapText(true); note.setStyle("-fx-text-fill: #5c6b7d;");
        Button open = new Button("Open account");
        open.setOnAction(event -> act(() -> {
            controller.openAccount(type.getValue(), opening.getText(), branch.getText());
            status.setText("Account opened successfully"); refresh();
        }));
        return new VBox(9, label("Account type"), type, label("Initial amount (BWP)"), opening,
                label("Branch"), branch, note, open);
    }

    private VBox actionForm() {
        TextField amount = new TextField(); amount.setPromptText("Amount, e.g. 100.00");
        TextField reference = new TextField("Cash deposit");
        Button deposit = new Button("Deposit");
        deposit.setOnAction(event -> act(() -> {
            controller.deposit(selected(), amount.getText(), reference.getText());
            status.setText("Deposit completed"); refresh();
        }));
        Button withdraw = new Button("Withdraw");
        withdraw.setOnAction(event -> act(() -> {
            controller.withdraw(selected(), amount.getText());
            status.setText("Withdrawal completed"); refresh();
        }));
        Button interest = new Button("Pay this month's interest");
        interest.setOnAction(event -> act(() -> {
            var earned = controller.payInterest(selected());
            status.setText("Interest paid: BWP " + earned); refresh();
        }));
        HBox buttons = new HBox(12, deposit, withdraw);
        return new VBox(10, label("Amount (BWP)"), amount, label("Deposit reference"), reference, buttons, interest);
    }

    private Label label(String value) { Label label = new Label(value); label.setStyle("-fx-font-weight: bold;"); return label; }

    private String selected() {
        AccountSummary summary = accounts.getValue();
        if (summary == null) throw new IllegalArgumentException("Select an account first");
        return summary.number();
    }

    private void refresh() {
        String previous = accounts.getValue() == null ? "" : accounts.getValue().number();
        var latest = controller.accounts();
        accounts.setItems(FXCollections.observableArrayList(latest));
        accounts.getSelectionModel().select(latest.stream()
                .filter(a -> a.number().equals(previous)).findFirst().orElse(latest.isEmpty() ? null : latest.get(0)));
        refreshHistory();
    }

    private void refreshHistory() {
        AccountSummary selected = accounts.getValue();
        if (selected == null) { history.setText("No accounts yet. Open one to begin."); return; }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm").withZone(ZoneId.systemDefault());
        StringBuilder builder = new StringBuilder();
        builder.append(selected.type()).append("  ").append(selected.number()).append("\n");
        builder.append("Balance: BWP ").append(selected.balance()).append("\n\n");
        for (BankTransaction row : controller.history(selected.number())) {
            builder.append(formatter.format(row.occurredAt())).append("  ")
                    .append(String.format("%-10s", row.kind())).append(" P")
                    .append(row.amount()).append("  Balance P").append(row.balanceAfter())
                    .append("\n   ").append(row.reference()).append("\n");
        }
        history.setText(builder.toString());
    }

    private void act(Runnable action) {
        try { action.run(); }
        catch (RuntimeException error) {
            new Alert(Alert.AlertType.ERROR, error.getMessage(), ButtonType.OK).showAndWait();
        }
    }
}
