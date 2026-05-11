package views;

import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import models.ChatMessage;
import services.ChatApiService;
import state.SessionManager;
import utils.AlertHelper;
import java.util.List;

public class ChatView {

    private final HBox root;
    private final ChatApiService chatApi = new ChatApiService();
    private VBox messageArea;
    private TextField messageInput;
    private int selectedUserId = -1;

    public ChatView() {
        root = new HBox(0);
        root.setPrefHeight(600);

        // Left panel - conversation list
        VBox leftPanel = new VBox(0);
        leftPanel.setPrefWidth(280);
        leftPanel.setMinWidth(280);
        leftPanel.setStyle("-fx-border-color: #E2E8F0; -fx-border-width: 0 1 0 0;");

        Label chatTitle = new Label("\uD83D\uDCAC Messages");
        chatTitle.getStyleClass().add("subheading");
        chatTitle.setPadding(new Insets(16));

        // Start new chat section
        HBox newChatBox = new HBox(8);
        newChatBox.setPadding(new Insets(0, 16, 16, 16));
        TextField userIdField = new TextField();
        userIdField.setPromptText("User ID");
        userIdField.setPrefWidth(120);
        Button startChatBtn = new Button("Start Chat");
        startChatBtn.getStyleClass().addAll("button");
        startChatBtn.setStyle("-fx-padding: 8 12; -fx-font-size: 12px;");
        startChatBtn.setOnAction(e -> {
            try {
                int userId = Integer.parseInt(userIdField.getText().trim());
                selectedUserId = userId;
                loadConversation(userId);
            } catch (NumberFormatException ex) {
                AlertHelper.showError("Invalid Input", "Enter a valid user ID.");
            }
        });
        newChatBox.getChildren().addAll(userIdField, startChatBtn);

        ScrollPane chatListScroll = new ScrollPane();
        chatListScroll.setFitToWidth(true);
        chatListScroll.setStyle("-fx-background-color: transparent;");
        VBox chatListContent = new VBox(0);

        // Sample chat entries
        for (int i = 1; i <= 5; i++) {
            final int uid = i;
            HBox chatItem = new HBox(10);
            chatItem.getStyleClass().add("chat-list-item");
            chatItem.setPadding(new Insets(12, 16, 12, 16));
            chatItem.setAlignment(Pos.CENTER_LEFT);

            Label avatar = new Label("\uD83D\uDC64");
            avatar.setStyle("-fx-font-size: 24px;");
            VBox chatInfo = new VBox(2);
            Label userName = new Label("User #" + i);
            userName.setStyle("-fx-font-weight: bold;");
            Label lastMsg = new Label("Click to load conversation");
            lastMsg.setStyle("-fx-text-fill: #64748B; -fx-font-size: 12px;");
            chatInfo.getChildren().addAll(userName, lastMsg);

            chatItem.getChildren().addAll(avatar, chatInfo);
            chatItem.setOnMouseClicked(e -> {
                selectedUserId = uid;
                loadConversation(uid);
            });
            chatListContent.getChildren().add(chatItem);
        }

        chatListScroll.setContent(chatListContent);
        VBox.setVgrow(chatListScroll, Priority.ALWAYS);
        leftPanel.getChildren().addAll(chatTitle, newChatBox, chatListScroll);

        // Right panel - message thread
        VBox rightPanel = new VBox(0);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);

        // Header
        HBox chatHeader = new HBox(12);
        chatHeader.setPadding(new Insets(16));
        chatHeader.setAlignment(Pos.CENTER_LEFT);
        chatHeader.setStyle("-fx-border-color: #E2E8F0; -fx-border-width: 0 0 1 0;");
        Label headerTitle = new Label("Select a conversation");
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
        placeholder.setStyle("-fx-text-fill: #64748B; -fx-font-size: 14px; -fx-padding: 40;");
        messageArea.getChildren().add(placeholder);

        // Input bar
        HBox inputBar = new HBox(12);
        inputBar.setPadding(new Insets(12, 16, 12, 16));
        inputBar.setAlignment(Pos.CENTER);
        inputBar.setStyle("-fx-border-color: #E2E8F0; -fx-border-width: 1 0 0 0;");

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
    }

    private void loadConversation(int otherUserId) {
        int myId = SessionManager.getInstance().getCurrentUser() != null ? SessionManager.getInstance().getCurrentUser().getUserId() : -1;
        new Thread(() -> {
            List<ChatMessage> messages = chatApi.getConversation(myId, otherUserId);
            javafx.application.Platform.runLater(() -> {
                messageArea.getChildren().clear();
                if (messages.isEmpty()) {
                    Label empty = new Label("No messages yet. Start the conversation!");
                    empty.setStyle("-fx-text-fill: #64748B;");
                    messageArea.getChildren().add(empty);
                } else {
                    for (ChatMessage msg : messages) {
                        boolean isMine = msg.getSenderId() == myId;
                        HBox bubble = new HBox();
                        Label msgLabel = new Label(msg.getContent());
                        msgLabel.setWrapText(true);
                        msgLabel.setMaxWidth(400);
                        msgLabel.getStyleClass().add(isMine ? "chat-bubble-sent" : "chat-bubble-received");
                        msgLabel.setStyle(msgLabel.getStyle() + (isMine ? "-fx-text-fill: white;" : ""));
                        bubble.setAlignment(isMine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
                        bubble.getChildren().add(msgLabel);
                        messageArea.getChildren().add(bubble);
                    }
                }
            });
        }).start();
    }

    private void sendMessage() {
        if (selectedUserId == -1) { AlertHelper.showError("Error", "Select a conversation first."); return; }
        String content = messageInput.getText().trim();
        if (content.isEmpty()) return;
        int myId = SessionManager.getInstance().getCurrentUser() != null ? SessionManager.getInstance().getCurrentUser().getUserId() : -1;

        new Thread(() -> {
            String result = chatApi.sendMessage(myId, selectedUserId, content);
            javafx.application.Platform.runLater(() -> {
                if (!result.startsWith("ERROR")) {
                    messageInput.clear();
                    loadConversation(selectedUserId);
                } else {
                    AlertHelper.showError("Error", result);
                }
            });
        }).start();
    }

    public HBox getRoot() { return root; }
}
