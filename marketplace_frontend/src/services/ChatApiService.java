package services;

import models.ChatMessage;
import network.SocketClient;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.ArrayList;
import java.util.List;

public class ChatApiService {

    private final SocketClient client = SocketClient.getInstance();

    public String startChat(int user1Id, int user2Id) {
        JSONObject payload = new JSONObject();
        payload.put("user1Id", user1Id);
        payload.put("user2Id", user2Id);

        String response = client.sendRequest("CHAT", "START_CHAT", payload);
        int code = SocketClient.getStatusCode(response);
        JSONObject body = SocketClient.getResponseBody(response);

        if (code == 200) {
            return body.optString("message", "Chat started");
        }
        return "ERROR: " + body.optString("error", "Failed to start chat");
    }

    public String sendMessage(int senderId, int receiverId, String content) {
        JSONObject payload = new JSONObject();
        payload.put("senderId", senderId);
        payload.put("receiverId", receiverId);
        payload.put("content", content);

        String response = client.sendRequest("CHAT", "SEND_MESSAGE", payload);
        int code = SocketClient.getStatusCode(response);
        JSONObject body = SocketClient.getResponseBody(response);

        if (code == 200) {
            return body.optString("message", "Message sent");
        }
        return "ERROR: " + body.optString("error", "Failed to send message");
    }

    public List<ChatMessage> getConversation(int user1Id, int user2Id) {
        JSONObject payload = new JSONObject();
        payload.put("user1Id", user1Id);
        payload.put("user2Id", user2Id);

        String response = client.sendRequest("CHAT", "GET_CONVERSATION", payload);
        int code = SocketClient.getStatusCode(response);
        List<ChatMessage> messages = new ArrayList<>();

        if (code == 200) {
            JSONObject body = SocketClient.getResponseBody(response);
            JSONArray arr = body.optJSONArray("messages");
            if (arr != null) {
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject m = arr.getJSONObject(i);
                    ChatMessage msg = new ChatMessage();
                    msg.setMessageId(m.optInt("messageId", 0));
                    msg.setSenderId(m.optInt("senderId", 0));
                    msg.setReceiverId(m.optInt("receiverId", 0));
                    msg.setContent(m.optString("content", ""));
                    msg.setRead(m.optBoolean("isRead", false));
                    msg.setTimestamp(m.optString("timestamp", ""));
                    messages.add(msg);
                }
            }
        }
        return messages;
    }

    public JSONObject listUserChats(int userId) {
        JSONObject payload = new JSONObject();
        payload.put("userId", userId);

        String response = client.sendRequest("CHAT", "LIST_USER_CHATS", payload);
        int code = SocketClient.getStatusCode(response);
        if (code == 200) {
            return SocketClient.getResponseBody(response);
        }
        return null;
    }
}
