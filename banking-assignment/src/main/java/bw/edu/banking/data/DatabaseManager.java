package bw.edu.banking.data;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/** Creates a local SQLite database and initialises its tables. */
public final class DatabaseManager {
    private final String jdbcUrl;

    public DatabaseManager(Path path) {
        this.jdbcUrl = "jdbc:sqlite:" + path.toAbsolutePath();
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException("SQLite JDBC library is missing", exception);
        }
    }

    public Connection connect() throws SQLException {
        Connection connection = DriverManager.getConnection(jdbcUrl);
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
            statement.execute("PRAGMA busy_timeout = 5000");
        }
        return connection;
    }

    public void initialise() {
        try (InputStream input = DatabaseManager.class.getResourceAsStream("/schema.sql")) {
            if (input == null) throw new IllegalStateException("Missing schema.sql resource");
            String script = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            try (Connection connection = connect(); Statement statement = connection.createStatement()) {
                for (String sql : script.split(";")) {
                    if (!sql.isBlank()) statement.execute(sql.strip());
                }
            }
        } catch (IOException | SQLException exception) {
            throw new IllegalStateException("Cannot create database", exception);
        }
    }
}
