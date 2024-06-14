package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

import Model.Account;
import Util.ConnectionUtil;

public class AccountDAO {

    public Optional<Account> getAccountByUsername(String username) throws DaoException {
        String sql = "SELECT * FROM Account WHERE username = ?";
        try (Connection connection = ConnectionUtil.getConnection(); PreparedStatement stmt = connection.prepareStatement(sql)) {
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
        try (Connection connection = ConnectionUtil.getConnection(); PreparedStatement stmt = connection.prepareStatement(sql)) {
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

    public boolean doesUsernameExist(String username) throws DaoException {
        String sql = "SELECT COUNT(*) FROM Account WHERE username = ?";
        try (Connection connection = ConnectionUtil.getConnection(); PreparedStatement stmt = connection.prepareStatement(sql)) {
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
        try (Connection connection = ConnectionUtil.getConnection(); PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
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
