package Microservices;

import DAOs.ChatMessageDAO;
import Entities.ChatMessageEntity;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class ChatService {

    private final ChatMessageDAO chatMessageDao;

    public ChatService() {
        this.chatMessageDao = new ChatMessageDAO();
    }

    public String sendMessage(String jsonPayload) {
        try {
            JSONObject json = new JSONObject(jsonPayload != null ? jsonPayload : "{}");

            int senderId = json.optInt("senderId", -1);
            int receiverId = json.optInt("receiverId", -1);
            String content = json.optString("content", "");

            if (senderId == -1 || receiverId == -1) {
                return "400 {\"error\":\"Missing senderId or receiverId\"}";
            }
            if (content.trim().isEmpty()) {
                return "400 {\"error\":\"Message content cannot be empty\"}";
            }
            if (senderId == receiverId) {
                return "400 {\"error\":\"Cannot send a message to yourself\"}";
            }

            boolean success = chatMessageDao.insertMessage(senderId, receiverId, content);

            if (success) {
                JSONObject res = new JSONObject();
                res.put("message", "Message sent successfully");
                return "200 " + res.toString();
            } else {
                return "400 {\"error\":\"Failed to send message\"}";
            }

        } catch (Exception e) {
            return "400 {\"error\":\"Invalid request: " + e.getMessage() + "\"}";
        }
    }

    public String getConversation(String jsonPayload) {
        try {
            JSONObject json = new JSONObject(jsonPayload != null ? jsonPayload : "{}");

            int user1Id = json.optInt("user1Id", -1);
            int user2Id = json.optInt("user2Id", -1);

            if (user1Id == -1 || user2Id == -1) {
                return "400 {\"error\":\"Missing user1Id or user2Id\"}";
            }

            List<ChatMessageEntity> messages = chatMessageDao.getConversation(user1Id, user2Id);

            JSONArray msgArray = new JSONArray();
            for (ChatMessageEntity msg : messages) {
                JSONObject msgObj = new JSONObject();
                msgObj.put("messageId", msg.getMessageId());
                msgObj.put("senderId", msg.getSenderId());
                msgObj.put("receiverId", msg.getReceiverId());
                msgObj.put("content", msg.getContent());
                msgObj.put("isRead", msg.isRead());
                msgObj.put("timestamp", msg.getTimestamp());
                msgArray.put(msgObj);
            }

            JSONObject res = new JSONObject();
            res.put("messages", msgArray);
            return "200 " + res.toString();

        } catch (Exception e) {
            return "400 {\"error\":\"Invalid request: " + e.getMessage() + "\"}";
        }
    }

    public String startChat(String jsonPayload) {
        try {
            JSONObject json = new JSONObject(jsonPayload != null ? jsonPayload : "{}");

            int user1Id = json.optInt("user1Id", -1);
            int user2Id = json.optInt("user2Id", -1);

            if (user1Id == -1 || user2Id == -1) {
                return "400 {\"error\":\"Missing user1Id or user2Id\"}";
            }

            JSONObject res = new JSONObject();
            res.put("message", "Chat session started between user " + user1Id + " and user " + user2Id);
            return "200 " + res.toString();

        } catch (Exception e) {
            return "400 {\"error\":\"Invalid request: " + e.getMessage() + "\"}";
        }
    }

    public String listUserChats(String jsonPayload) {
        try {
            JSONObject json = new JSONObject(jsonPayload != null ? jsonPayload : "{}");

            int userId = json.optInt("userId", -1);

            if (userId == -1) {
                return "400 {\"error\":\"Missing userId\"}";
            }

            JSONObject res = new JSONObject();
            res.put("message", "Chat list retrieved for user " + userId);
            res.put("chats", new JSONArray());
            return "200 " + res.toString();

        } catch (Exception e) {
            return "400 {\"error\":\"Invalid request: " + e.getMessage() + "\"}";
        }
    }
}
