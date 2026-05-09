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
