package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import Model.Message;
import Util.ConnectionUtil;

public class MessageDAO {

    public Message createMessage(Message message) throws DaoException {
        String sql = "INSERT INTO message (posted_by, message_text, time_posted_epoch) VALUES (?, ?, ?)";
        try (Connection conn = ConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, message.getPosted_by());
            ps.setString(2, message.getMessage_text());
            ps.setLong(3, message.getTime_posted_epoch());
            ps.executeUpdate();
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    return new Message(generatedId, message.getPosted_by(), message.getMessage_text(), message.getTime_posted_epoch());
                } else {
                    throw new DaoException("Failed to insert message, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            handleSQLException(e, sql, "Error while inserting a message");
        }
        throw new DaoException("Failed to insert message");
    }

    public List<Message> getAllMessages() throws DaoException {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT * FROM Message";
        try (Connection connection = ConnectionUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                messages.add(new Message(rs.getInt("message_id"), rs.getInt("posted_by"), rs.getString("message_text"), rs.getLong("time_posted_epoch")));
            }
        } catch (SQLException e) {
            handleSQLException(e, sql, "Error while fetching all messages");
        }
        return messages;
    }

    public Optional<Message> getMessageById(int messageId) throws DaoException {
        String sql = "SELECT * FROM Message WHERE message_id = ?";
        try (Connection connection = ConnectionUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, messageId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Message(rs.getInt("message_id"), rs.getInt("posted_by"), rs.getString("message_text"), rs.getLong("time_posted_epoch")));
                }
            }
        } catch (SQLException e) {
            handleSQLException(e, sql, "Error while fetching message by ID");
        }
        return Optional.empty();
    }

    public boolean deleteMessage(int messageId) throws DaoException {
        String sql = "DELETE FROM Message WHERE message_id = ?";
        try (Connection connection = ConnectionUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, messageId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            handleSQLException(e, sql, "Error while deleting message");
        }
        return false;
    }

    public boolean delete(Message message) throws DaoException {
        return deleteMessage(message.getMessage_id());
    }

    public boolean update(Message message) throws DaoException {
        String sql = "UPDATE Message SET message_text = ? WHERE message_id = ?";
        try (Connection connection = ConnectionUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, message.getMessage_text());
            stmt.setInt(2, message.getMessage_id());
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            handleSQLException(e, sql, "Error while updating message");
        }
        return false;
    }

    public List<Message> getMessagesByAccountId(int accountId) throws DaoException {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT * FROM Message WHERE posted_by = ?";
        try (Connection connection = ConnectionUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    messages.add(new Message(rs.getInt("message_id"), rs.getInt("posted_by"), rs.getString("message_text"), rs.getLong("time_posted_epoch")));
                }
            }
        } catch (SQLException e) {
            handleSQLException(e, sql, "Error while fetching messages by account ID");
        }
        return messages;
    }

    private void handleSQLException(SQLException e, String sql, String message) throws DaoException {
        System.err.println(message + " - SQL: " + sql);
        e.printStackTrace();
        throw new DaoException(message, e);
    }
}
