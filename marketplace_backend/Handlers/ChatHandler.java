package Handlers;
import Handlers.ServiceHandler;
import Microservices.ChatService;
import org.json.JSONObject;

public class ChatHandler implements ServiceHandler {

    private final ChatService chatService;

    public ChatHandler() {
        this.chatService = new ChatService();
    }

    @Override
    public String handleRequest(String action, String jsonPayload) {

        JSONObject json;
        try {
            json = new JSONObject(jsonPayload != null ? jsonPayload : "{}");
        } catch (Exception e) {
            return "400 {\"error\":\"Invalid request: " + e.getMessage() + "\"}";
        }

        switch (action.toUpperCase()) {

            case "START_CHAT": {
                int user1Id = json.optInt("user1Id", -1);
                int user2Id = json.optInt("user2Id", -1);
                return chatService.startChat(user1Id, user2Id);
            }

            case "SEND_MESSAGE": {
                int senderId = json.optInt("senderId", -1);
                int receiverId = json.optInt("receiverId", -1);
                String content = json.optString("content", "");
                return chatService.sendMessage(senderId, receiverId, content);
            }

            case "GET_CONVERSATION": {
                int user1Id = json.optInt("user1Id", -1);
                int user2Id = json.optInt("user2Id", -1);
                return chatService.getConversation(user1Id, user2Id);
            }

            case "LIST_USER_CHATS": {
                int userId = json.optInt("userId", -1);
                return chatService.listUserChats(userId);
            }

            default:
                return "400 {\"error\":\"Unknown Chat Action: " + action + "\"}";
        }
    }
}