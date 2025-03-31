package com.beginsecure.application_good.controller;

import com.beginsecure.application_good.domain.Message;
import com.beginsecure.application_good.domain.User;
import com.beginsecure.application_good.service.Observer;
import com.beginsecure.application_good.service.Service;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class ChatController implements Observer<Message> {
    private Service service;
    private User loggedInUser;
    private User chattingWithUser;

    @FXML
    private ListView<String> messageListView;

    @FXML
    private TextField messageField;

    private ObservableList<String> messages = FXCollections.observableArrayList();

    public void setService(Service service) {
        this.service = service;
    }

    public void setLoggedInUser(User loggedInUser) {
        this.loggedInUser = loggedInUser;
    }

    public void setChatPartner(User chatPartner) {
        this.chattingWithUser = chatPartner;
        loadConversation();
    }

    private void loadConversation() {
        messages.clear();
        List<Message> conversation = service.getConversationBetweenUsers(loggedInUser.getId(), chattingWithUser.getId());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        for (Message message : conversation) {
            String formattedMessage = formatMessage(message, formatter);
            messages.add(formattedMessage);
        }

        messageListView.setItems(messages);
    }

    private String formatMessage(Message message, DateTimeFormatter formatter) {
        String sender = message.getFrom().equals(loggedInUser) ? "You" : chattingWithUser.getFirstName();
        String time = message.getDate().format(formatter);
        return sender + " (" + time + "): " + message.getMessage();
    }

    @FXML
    private void handleSendMessage() {
        try {
            String messageText = messageField.getText().trim();
            if (messageText.isEmpty()) {
                throw new IllegalArgumentException("Message cannot be empty.");
            }

            service.sendMessage(loggedInUser.getId(), List.of(chattingWithUser.getId()), messageText);

            messageField.clear();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error sending message: " + e.getMessage());
            alert.show();
        }
    }


    @Override
    public void update(Message message) {
        if (isRelevantMessage(message)) {
            Platform.runLater(() -> {
                String formattedMessage = formatMessage(message, DateTimeFormatter.ofPattern("HH:mm"));
                messages.add(formattedMessage);
                messageListView.scrollTo(messages.size() - 1);
            });
        }
    }

    private boolean isRelevantMessage(Message message) {
        Long senderId = message.getFrom().getId();
        List<Long> recipientIds = message.getTo().stream().map(User::getId).toList();
        return (senderId.equals(loggedInUser.getId()) && recipientIds.contains(chattingWithUser.getId())) ||
                (senderId.equals(chattingWithUser.getId()) && recipientIds.contains(loggedInUser.getId()));
    }
}
