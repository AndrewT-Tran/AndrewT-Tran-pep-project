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

    public Optional<Message> getMessageById(int id) throws DaoException {
        String sql = "SELECT * FROM message WHERE id = ?";
        try (Connection conn = ConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Message(
                        rs.getInt("id"),
                        rs.getInt("posted_by"),
                        rs.getString("message_text"),
                        rs.getLong("time_posted_epoch")));
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Error fetching message by ID", e);
        }
        return Optional.empty();
    }

    public List<Message> getAll() throws DaoException {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT * FROM message";
        try (Connection conn = ConnectionUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                messages.add(new Message(
                    rs.getInt("id"),
                    rs.getInt("posted_by"),
                    rs.getString("message_text"),
                    rs.getLong("time_posted_epoch")));
            }
        } catch (SQLException e) {
            throw new DaoException("Error fetching all messages", e);
        }
        return messages;
    }

    public List<Message> getMessagesByAccountId(int accountId) throws DaoException {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT * FROM message WHERE posted_by = ?";
        try (Connection conn = ConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    messages.add(new Message(
                        rs.getInt("id"),
                        rs.getInt("posted_by"),
                        rs.getString("message_text"),
                        rs.getLong("time_posted_epoch")));
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Error fetching messages by account ID", e);
        }
        return messages;
    }

    public Message insert(Message message) throws DaoException {
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
            throw new DaoException("Error while inserting a message", e);
        }
    }

    public void update(Message message) throws DaoException {
        String sql = "UPDATE message SET message_text = ? WHERE id = ?";
        try (Connection conn = ConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, message.getMessage_text());
            ps.setInt(2, message.getMessage_id());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Error updating message", e);
        }
    }

    public boolean delete(Message message) throws DaoException {
        String sql = "DELETE FROM message WHERE id = ?";
        try (Connection conn = ConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, message.getMessage_id());
            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            throw new DaoException("Error deleting message", e);
        }
    }
}
