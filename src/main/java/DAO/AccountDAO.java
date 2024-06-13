package DAO;

import Model.Account;
import Util.ConnectionUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class AccountDAO {

    public Optional<Account> getAccountByUsername(String username) throws DaoException {
        String sql = "SELECT * FROM Account WHERE username = ?";
        try (Connection connection = ConnectionUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Account(rs.getInt("account_id"), rs.getString("username"), rs.getString("password")));
                }
            }
        } catch (SQLException e) {
            handleSQLException(e, sql, "Error while fetching account by username");
        }
        return Optional.empty();
    }

    public Optional<Account> getAccountById(int accountId) throws DaoException {
        String sql = "SELECT * FROM Account WHERE account_id = ?";
        try (Connection connection = ConnectionUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Account(rs.getInt("account_id"), rs.getString("username"), rs.getString("password")));
                }
            }
        } catch (SQLException e) {
            handleSQLException(e, sql, "Error while fetching account by ID");
        }
        return Optional.empty();
    }

    public List<Account> getAll() throws DaoException {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT * FROM Account";
        try (Connection connection = ConnectionUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                accounts.add(new Account(rs.getInt("account_id"), rs.getString("username"), rs.getString("password")));
            }
        } catch (SQLException e) {
            handleSQLException(e, sql, "Error while fetching all accounts");
        }
        return accounts;
    }

    public boolean update(Account account) throws DaoException {
        String sql = "UPDATE Account SET username = ?, password = ? WHERE account_id = ?";
        try (Connection conn = ConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, account.getUsername());
            ps.setString(2, account.getPassword());
            ps.setInt(3, account.getAccount_id());
            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                return true;
            } else {
                throw new DaoException("Updating account failed, no such account found.");
            }
        } catch (SQLException e) {
            throw new DaoException("Updating account failed due to SQL error", e);
        }
    }

    public boolean doesUsernameExist(String username) throws DaoException {
        String sql = "SELECT COUNT(*) FROM Account WHERE username = ?";
        try (Connection connection = ConnectionUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            handleSQLException(e, sql, "Error while checking if username exists");
        }
        return false;
    }

    public Account createAccount(Account account) throws DaoException {
        String sql = "INSERT INTO Account (username, password) VALUES (?, ?)";
        try (Connection connection = ConnectionUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, account.getUsername());
            stmt.setString(2, account.getPassword());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    account.setAccount_id(rs.getInt(1));
                    return account;
                } else {
                    throw new DaoException("Creating account failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Creating account failed due to SQL error", e);
        }
    }

    private void handleSQLException(SQLException e, String sql, String message) throws DaoException {
        System.err.println(message + " - SQL: " + sql);
        e.printStackTrace();
        throw new DaoException(message, e);
    }
}
