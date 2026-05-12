package Microservices;

import DAOs.ChatMessageDAO;
import DAOs.UserDAO;
import Entities.ChatMessageEntity;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class ChatService {

    private final ChatMessageDAO chatMessageDao;
    private final UserDAO userDao;

    public ChatService() {
        this.chatMessageDao = new ChatMessageDAO();
        this.userDao = new UserDAO();
    }

    public String sendMessage(int senderId, int receiverId, String content) {
        try {
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

    public String getConversation(int user1Id, int user2Id) {
        try {
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

    public String startChat(int user1Id, int user2Id) {
        try {
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

    public String listUserChats(int userId) {
        try {
            if (userId == -1) {
                return "400 {\"error\":\"Missing userId\"}";
            }

            List<int[]> partners = chatMessageDao.getDistinctConversations(userId);
            JSONArray chats = new JSONArray();

            for (int[] partner : partners) {
                int partnerId = partner[0];
                JSONObject chatObj = new JSONObject();
                chatObj.put("partnerId", partnerId);
                chatObj.put("partnerName", userDao.getUsernameById(partnerId));

                int unread = chatMessageDao.getUnreadCount(userId, partnerId);
                chatObj.put("unreadCount", unread);

                ChatMessageEntity lastMsg = chatMessageDao.getLastMessage(userId, partnerId);
                if (lastMsg != null) {
                    chatObj.put("lastMessage", lastMsg.getContent());
                    chatObj.put("lastMessageTime", lastMsg.getTimestamp());
                    chatObj.put("lastMessageSenderId", lastMsg.getSenderId());
                } else {
                    chatObj.put("lastMessage", "");
                    chatObj.put("lastMessageTime", "");
                    chatObj.put("lastMessageSenderId", 0);
                }

                chats.put(chatObj);
            }

            JSONObject res = new JSONObject();
            res.put("chats", chats);
            return "200 " + res.toString();

        } catch (Exception e) {
            return "400 {\"error\":\"Invalid request: " + e.getMessage() + "\"}";
        }
    }

    public String markAsRead(int userId, int fromUserId) {
        try {
            boolean success = chatMessageDao.markAsRead(userId, fromUserId);
            if (success) {
                return "200 {\"message\":\"Messages marked as read\"}";
            }
            return "400 {\"error\":\"Failed to mark messages as read\"}";
        } catch (Exception e) {
            return "400 {\"error\":\"Invalid request: " + e.getMessage() + "\"}";
        }
    }
}
