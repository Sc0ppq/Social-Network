package com.beginsecure.application_good.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.util.List;

public class FriendRequestsController {

    @FXML
    private ListView<String> friendRequestsList;

    @FXML
    private Button closeButton;

    public void setFriendRequests(List<String> friendRequests) {
        friendRequestsList.getItems().setAll(friendRequests);
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}
