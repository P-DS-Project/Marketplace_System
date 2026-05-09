package Handlers;
import Handlers.ServiceHandler;
import Microservices.ChatService;

public class ChatHandler implements ServiceHandler {

    private final ChatService chatService;

    public ChatHandler() {
        this.chatService = new ChatService();
    }

    @Override
    public String handleRequest(String action, String jsonPayload) {

        switch (action.toUpperCase()) {

            case "START_CHAT":
                return chatService.startChat(jsonPayload);

            case "SEND_MESSAGE":
                return chatService.sendMessage(jsonPayload);

            case "GET_CONVERSATION":
                return chatService.getConversation(jsonPayload);

            case "LIST_USER_CHATS":
                return chatService.listUserChats(jsonPayload);

            default:
                return "400 {\"error\":\"Unknown Chat Action: " + action + "\"}";
        }
    }
}