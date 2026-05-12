package DAOs;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import Entities.ChatMessageEntity;
import Utils.DatabaseConnectionManager;

public class ChatMessageDAO {

    private Connection getConnection() throws SQLException {
        return DatabaseConnectionManager.getNode1UsersConnection();
    }

    public boolean insertMessage(int senderId, int receiverId, String content) {
        String sql = "INSERT INTO chat_messages (sender_id, receiver_id, content, is_read) VALUES (?, ?, ?, false)";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, senderId);
            pstmt.setInt(2, receiverId);
            pstmt.setString(3, content);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Database error inserting chat message from " + senderId + " to " + receiverId);
            e.printStackTrace();
            return false;
        }
    }

    public List<ChatMessageEntity> getConversation(int user1Id, int user2Id) {
        List<ChatMessageEntity> messages = new ArrayList<>();
        String sql = "SELECT message_id, sender_id, receiver_id, content, is_read, timestamp " +
                     "FROM chat_messages " +
                     "WHERE (sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?) " +
                     "ORDER BY timestamp ASC";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, user1Id);
            pstmt.setInt(2, user2Id);
            pstmt.setInt(3, user2Id);
            pstmt.setInt(4, user1Id);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    messages.add(mapRowToMessage(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error fetching conversation between " + user1Id + " and " + user2Id);
            e.printStackTrace();
        }
        return messages;
    }

    public List<int[]> getDistinctConversations(int userId) {
        List<int[]> partners = new ArrayList<>();
        String sql = "SELECT DISTINCT CASE WHEN sender_id = ? THEN receiver_id ELSE sender_id END as partner_id, " +
                     "MAX(timestamp) as last_msg_time " +
                     "FROM chat_messages WHERE sender_id = ? OR receiver_id = ? " +
                     "GROUP BY partner_id ORDER BY last_msg_time DESC";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, userId);
            pstmt.setInt(3, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    partners.add(new int[]{rs.getInt("partner_id")});
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return partners;
    }

    public int getUnreadCount(int userId, int fromUserId) {
        String sql = "SELECT COUNT(*) FROM chat_messages WHERE sender_id = ? AND receiver_id = ? AND is_read = false";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, fromUserId);
            pstmt.setInt(2, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public ChatMessageEntity getLastMessage(int user1Id, int user2Id) {
        String sql = "SELECT message_id, sender_id, receiver_id, content, is_read, timestamp " +
                     "FROM chat_messages " +
                     "WHERE (sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?) " +
                     "ORDER BY timestamp DESC LIMIT 1";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, user1Id);
            pstmt.setInt(2, user2Id);
            pstmt.setInt(3, user2Id);
            pstmt.setInt(4, user1Id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapRowToMessage(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean markAsRead(int userId, int fromUserId) {
        String sql = "UPDATE chat_messages SET is_read = true WHERE sender_id = ? AND receiver_id = ? AND is_read = false";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, fromUserId);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private ChatMessageEntity mapRowToMessage(ResultSet rs) throws SQLException {
        ChatMessageEntity msg = new ChatMessageEntity();
        msg.setMessageId(rs.getInt("message_id"));
        msg.setSenderId(rs.getInt("sender_id"));
        msg.setReceiverId(rs.getInt("receiver_id"));
        msg.setContent(rs.getString("content"));
        msg.setRead(rs.getBoolean("is_read"));
        msg.setTimestamp(rs.getString("timestamp"));
        return msg;
    }
}
