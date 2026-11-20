package bw.edu.banking.data;

import bw.edu.banking.model.Customer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/** Database queries belong in DAO classes, never in a GUI class. */
public final class CustomerDao {
    public void insert(Connection connection, Customer customer) throws SQLException {
        String sql = "INSERT INTO customers (id,first_name,surname,address,email,password_hash,employer,employer_address) VALUES (?,?,?,?,?,?,?,?)";
        try (PreparedStatement query = connection.prepareStatement(sql)) {
            query.setString(1, customer.getId());
            query.setString(2, customer.getFirstName());
            query.setString(3, customer.getSurname());
            query.setString(4, customer.getAddress());
            query.setString(5, customer.getEmail());
            query.setString(6, customer.getPasswordHash());
            query.setString(7, customer.getEmployer());
            query.setString(8, customer.getEmployerAddress());
            query.executeUpdate();
        }
    }

    public Optional<Customer> findByEmail(Connection connection, String email) throws SQLException {
        try (PreparedStatement query = connection.prepareStatement("SELECT * FROM customers WHERE email = ?")) {
            query.setString(1, email);
            try (ResultSet results = query.executeQuery()) {
                return results.next() ? Optional.of(map(results)) : Optional.empty();
            }
        }
    }

    public Optional<Customer> findById(Connection connection, String id) throws SQLException {
        try (PreparedStatement query = connection.prepareStatement("SELECT * FROM customers WHERE id = ?")) {
            query.setString(1, id);
            try (ResultSet results = query.executeQuery()) {
                return results.next() ? Optional.of(map(results)) : Optional.empty();
            }
        }
    }

    public void updateContact(Connection connection, String id, String address) throws SQLException {
        try (PreparedStatement query = connection.prepareStatement("UPDATE customers SET address = ? WHERE id = ?")) {
            query.setString(1, address);
            query.setString(2, id);
            query.executeUpdate();
        }
    }

    public void delete(Connection connection, String id) throws SQLException {
        try (PreparedStatement query = connection.prepareStatement("DELETE FROM customers WHERE id = ?")) {
            query.setString(1, id);
            query.executeUpdate();
        }
    }

    private Customer map(ResultSet row) throws SQLException {
        return new Customer(row.getString("id"), row.getString("first_name"), row.getString("surname"),
                row.getString("address"), row.getString("email"), row.getString("password_hash"),
                row.getString("employer"), row.getString("employer_address"));
    }
}
