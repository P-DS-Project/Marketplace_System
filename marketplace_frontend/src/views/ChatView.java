package views;

import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import models.ChatMessage;
import services.ChatApiService;
import state.SessionManager;
import utils.AlertHelper;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.List;

public class ChatView {

    private final HBox root;
    private final ChatApiService chatApi = new ChatApiService();
    private VBox messageArea;
    private TextField messageInput;
    private int selectedUserId = -1;
    private Label headerTitle;
    private VBox chatListContent;

    public ChatView(int initialUserId) {
        root = new HBox(0);
        root.setPrefHeight(600);

        // Left panel - conversation list
        VBox leftPanel = new VBox(0);
        leftPanel.setPrefWidth(300);
        leftPanel.setMinWidth(300);
        leftPanel.setStyle("-fx-border-color: -border-color; -fx-border-width: 0 1 0 0;");

        Label chatTitle = new Label("\uD83D\uDCAC Messages");
        chatTitle.getStyleClass().add("subheading");
        chatTitle.setPadding(new Insets(16));

        // Search conversations
        TextField searchField = new TextField();
        searchField.setPromptText("\uD83D\uDD0D Search conversations...");
        searchField.setPadding(new Insets(8));
        HBox searchBox = new HBox();
        searchBox.setPadding(new Insets(0, 16, 12, 16));
        searchBox.getChildren().add(searchField);
        HBox.setHgrow(searchField, Priority.ALWAYS);

        ScrollPane chatListScroll = new ScrollPane();
        chatListScroll.setFitToWidth(true);
        chatListScroll.setStyle("-fx-background-color: transparent;");
        chatListContent = new VBox(0);
        chatListScroll.setContent(chatListContent);
        VBox.setVgrow(chatListScroll, Priority.ALWAYS);

        leftPanel.getChildren().addAll(chatTitle, searchBox, chatListScroll);

        // Right panel - message thread
        VBox rightPanel = new VBox(0);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);

        // Header
        HBox chatHeader = new HBox(12);
        chatHeader.setPadding(new Insets(16));
        chatHeader.setAlignment(Pos.CENTER_LEFT);
        chatHeader.setStyle("-fx-border-color: -border-color; -fx-border-width: 0 0 1 0;");
        headerTitle = new Label("Select a conversation");
        headerTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        chatHeader.getChildren().add(headerTitle);

        // Message area
        messageArea = new VBox(8);
        messageArea.setPadding(new Insets(16));
        ScrollPane msgScroll = new ScrollPane(messageArea);
        msgScroll.setFitToWidth(true);
        msgScroll.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(msgScroll, Priority.ALWAYS);

        Label placeholder = new Label("\uD83D\uDCAC Select or start a conversation to begin chatting");
        placeholder.setStyle("-fx-text-fill: -text-subtle; -fx-font-size: 14px; -fx-padding: 40;");
        messageArea.getChildren().add(placeholder);

        // Input bar
        HBox inputBar = new HBox(12);
        inputBar.setPadding(new Insets(12, 16, 12, 16));
        inputBar.setAlignment(Pos.CENTER);
        inputBar.setStyle("-fx-border-color: -border-color; -fx-border-width: 1 0 0 0;");

        messageInput = new TextField();
        messageInput.getStyleClass().add("chat-input");
        messageInput.setPromptText("Type a message...");
        HBox.setHgrow(messageInput, Priority.ALWAYS);
        messageInput.setOnAction(e -> sendMessage());

        Button sendBtn = new Button("Send \u27A4");
        sendBtn.setOnAction(e -> sendMessage());

        inputBar.getChildren().addAll(messageInput, sendBtn);

        rightPanel.getChildren().addAll(chatHeader, msgScroll, inputBar);
        root.getChildren().addAll(leftPanel, rightPanel);

        // Load real chat list
        loadChatList();

        if (initialUserId != -1) {
            selectedUserId = initialUserId;
            headerTitle.setText("User #" + initialUserId);
            loadConversation(initialUserId);
        }

        // Filter conversations by search
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterChatList(newVal);
        });
    }

    private void loadChatList() {
        int myId = SessionManager.getInstance().getCurrentUser() != null
                ? SessionManager.getInstance().getCurrentUser().getUserId()
                : -1;
        if (myId == -1)
            return;

        new Thread(() -> {
            JSONObject result = chatApi.listUserChats(myId);
            javafx.application.Platform.runLater(() -> {
                chatListContent.getChildren().clear();

                if (result == null || !result.has("chats")) {
                    Label empty = new Label("No conversations yet");
                    empty.setStyle("-fx-text-fill: #64748B; -fx-padding: 20 16;");
                    chatListContent.getChildren().add(empty);
                    return;
                }

                JSONArray chats = result.getJSONArray("chats");
                for (int i = 0; i < chats.length(); i++) {
                    JSONObject chat = chats.getJSONObject(i);
                    int partnerId = chat.optInt("partnerId");
                    String partnerName = chat.optString("partnerName", "User #" + partnerId);
                    String lastMessage = chat.optString("lastMessage", "");
                    int unreadCount = chat.optInt("unreadCount", 0);

                    chatListContent.getChildren().add(
                            createChatListItem(partnerId, partnerName, lastMessage, unreadCount));
                }
            });
        }).start();
    }

    private void filterChatList(String query) {
        if (query == null || query.trim().isEmpty()) {
            for (javafx.scene.Node child : chatListContent.getChildren()) {
                child.setVisible(true);
                child.setManaged(true);
            }
            return;
        }
        String lowerQuery = query.toLowerCase();
        for (javafx.scene.Node child : chatListContent.getChildren()) {
            if (child instanceof HBox) {
                HBox item = (HBox) child;
                String userData = item.getUserData() != null ? item.getUserData().toString().toLowerCase() : "";
                boolean match = userData.contains(lowerQuery);
                item.setVisible(match);
                item.setManaged(match);
            }
        }
    }

    private HBox createChatListItem(int partnerId, String partnerName, String lastMsg, int unreadCount) {
        HBox chatItem = new HBox(10);
        chatItem.getStyleClass().add("chat-list-item");
        chatItem.setPadding(new Insets(12, 16, 12, 16));
        chatItem.setAlignment(Pos.CENTER_LEFT);
        chatItem.setUserData(partnerName);

        Label avatar = new Label("\uD83D\uDC64");
        avatar.setStyle("-fx-font-size: 28px;");

        VBox chatInfo = new VBox(2);
        HBox.setHgrow(chatInfo, Priority.ALWAYS);

        HBox nameRow = new HBox(8);
        nameRow.setAlignment(Pos.CENTER_LEFT);
        Label userName = new Label(partnerName);
        userName.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        if (unreadCount > 0) {
            Label unreadBadge = new Label(String.valueOf(unreadCount));
            unreadBadge.setStyle("-fx-background-color: #3B82F6; -fx-text-fill: white; -fx-background-radius: 10; " +
                    "-fx-padding: 2 8; -fx-font-size: 11px; -fx-font-weight: bold;");
            nameRow.getChildren().addAll(userName, unreadBadge);
        } else {
            nameRow.getChildren().add(userName);
        }

        Label lastMsgLabel = new Label(lastMsg.length() > 40 ? lastMsg.substring(0, 37) + "..." : lastMsg);
        lastMsgLabel.setStyle("-fx-text-fill: #64748B; -fx-font-size: 12px;");

        chatInfo.getChildren().addAll(nameRow, lastMsgLabel);
        chatItem.getChildren().addAll(avatar, chatInfo);

        chatItem.setOnMouseClicked(e -> {
            selectedUserId = partnerId;
            headerTitle.setText(partnerName);
            loadConversation(partnerId);

            // Mark as read
            int myId = SessionManager.getInstance().getCurrentUser() != null
                    ? SessionManager.getInstance().getCurrentUser().getUserId()
                    : -1;
            if (myId != -1) {
                new Thread(() -> {
                    chatApi.markAsRead(myId, partnerId);
                    javafx.application.Platform.runLater(this::loadChatList);
                }).start();
            }
        });

        return chatItem;
    }

    private void loadConversation(int otherUserId) {
        int myId = SessionManager.getInstance().getCurrentUser() != null
                ? SessionManager.getInstance().getCurrentUser().getUserId()
                : -1;
        new Thread(() -> {
            List<ChatMessage> messages = chatApi.getConversation(myId, otherUserId);
            javafx.application.Platform.runLater(() -> {
                messageArea.getChildren().clear();
                if (messages.isEmpty()) {
                    Label empty = new Label("No messages yet. Start the conversation!");
                    empty.setStyle("-fx-text-fill: -text-subtle;");
                    messageArea.getChildren().add(empty);
                } else {
                    for (ChatMessage msg : messages) {
                        boolean isMine = msg.getSenderId() == myId;
                        VBox bubble = new VBox(2);

                        Label msgLabel = new Label(msg.getContent());
                        msgLabel.setWrapText(true);
                        msgLabel.setMaxWidth(400);
                        msgLabel.getStyleClass().add(isMine ? "chat-bubble-sent" : "chat-bubble-received");
                        if (isMine)
                            msgLabel.setStyle(msgLabel.getStyle() + "-fx-text-fill: white;");

                        Label timeLabel = new Label(msg.getTimestamp() != null ? msg.getTimestamp() : "");
                        timeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: -text-subtle;");

                        bubble.getChildren().addAll(msgLabel, timeLabel);
                        bubble.setAlignment(isMine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

                        HBox row = new HBox();
                        row.setAlignment(isMine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
                        row.getChildren().add(bubble);
                        messageArea.getChildren().add(row);
                    }
                }
            });
        }).start();
    }

    private void sendMessage() {
        if (selectedUserId == -1) {
            AlertHelper.showError("Error", "Select a conversation first.");
            return;
        }
        String content = messageInput.getText().trim();
        if (content.isEmpty())
            return;
        int myId = SessionManager.getInstance().getCurrentUser() != null
                ? SessionManager.getInstance().getCurrentUser().getUserId()
                : -1;

        new Thread(() -> {
            String result = chatApi.sendMessage(myId, selectedUserId, content);
            javafx.application.Platform.runLater(() -> {
                if (!result.startsWith("ERROR")) {
                    messageInput.clear();
                    loadConversation(selectedUserId);
                    loadChatList();
                } else {
                    AlertHelper.showError("Error", result);
                }
            });
        }).start();
    }

    public HBox getRoot() {
        return root;
    }
}
