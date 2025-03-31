package com.beginsecure.application_good.controller;

import com.beginsecure.application_good.domain.Page;
import com.beginsecure.application_good.domain.Pageable;
import com.beginsecure.application_good.domain.User;
import com.beginsecure.application_good.service.Service;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MainWindowController {
    private Service service;
    private User loggedInUser;
    private Pageable currentPageable = new Pageable(3, 0);

    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;

    @FXML
    private TableView<User> friendsTable;
    @FXML
    private TableView<User> allUsersTable;

    @FXML
    private TableColumn<User, String> friendFirstNameCol;
    @FXML
    private TableColumn<User, String> friendLastNameCol;
    @FXML
    private TableColumn<User, String> userFirstNameCol;
    @FXML
    private TableColumn<User, String> userLastNameCol;

    @FXML
    private Button prevPageButton;
    @FXML
    private Button nextPageButton;

    @FXML
    private Label pageInfoLabel;

    @FXML
    private TextField searchField;

    public void setService(Service service) {
        this.service = service;
    }

    @FXML
    private void handleLogin() {
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();

        Optional<User> user = service.findUserByName(firstName, lastName);



        if (user.isPresent()) {
            User loggedIn = user.get();
            List<User> friendRequests = service.getPendingRequests(loggedIn.getId());
            showFriendRequestsNotif(friendRequests);
            if (loggedInUser == null) {
                setLoggedInUser(user.get());
            } else {
                firstNameField.clear();
                lastNameField.clear();
                openNewWindow(user.get());
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "User not found!");
            alert.show();
        }
    }

    private void initializeData() {
        loadFriends();
        loadAllUsers();
    }

    @FXML
    private void handleDeleteFriend() {
        User selectedFriend = friendsTable.getSelectionModel().getSelectedItem();
        if (selectedFriend != null) {
            service.removeFriendship(loggedInUser.getId(), selectedFriend.getId());
            loadFriends();
        }
    }

    @FXML
    private void handleNewFriend() {
        User selectedUser = allUsersTable.getSelectionModel().getSelectedItem();

        if (selectedUser == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a user to send a friend request.");
            alert.show();
            return;
        }

        try {
            service.sendFriendRequest(loggedInUser.getId(), selectedUser.getId());
            Alert successAlert = new Alert(Alert.AlertType.INFORMATION, "Friend request sent successfully to "
                    + selectedUser.getFirstName() + " " + selectedUser.getLastName() + "!");
            successAlert.show();
        } catch (Exception e) {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Failed to send friend request: " + e.getMessage());
            errorAlert.show();
        }
    }

    @FXML
    private void handleShowFriendRequests() {
        List<String> friendRequests = service.getFormattedPendingRequests(loggedInUser.getId());
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Friend Requests");
        alert.setHeaderText("You have friend requests from:");
        alert.setContentText(friendRequests.toString());
        alert.show();
    }

    private void showFriendRequestsNotif(List<User> friendRequests) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/beginsecure/application_good/views/notification-view.fxml"));
            Parent root = loader.load();

            FriendRequestsController controller = loader.getController();


            List<String> friendRequestsStrings = friendRequests.stream()
                    .map(user -> user.getFirstName() + " " + user.getLastName())
                    .collect(Collectors.toList());

            controller.setFriendRequests(friendRequestsStrings);

            Stage stage = new Stage();
            stage.setTitle("Friend Requests");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    @FXML
    private void handleSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            loadAllUsers();
            return;
        }

        String[] nameParts = query.split("\\s+");
        String firstName = nameParts.length > 0 ? nameParts[0] : "";
        String lastName = nameParts.length > 1 ? nameParts[1] : "";

        Optional<User> user = service.findUserByName(firstName, lastName);
        if (user.isPresent()) {
            allUsersTable.getItems().setAll(user.get());
        } else {
            allUsersTable.getItems().clear();
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "User not found!");
            alert.show();
        }
    }

    public void openNewWindow(User newUser) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/beginsecure/application_good/views/main-view.fxml"));
            Scene newScene = new Scene(loader.load());

            MainWindowController newController = loader.getController();
            newController.setService(service);
            newController.setLoggedInUser(newUser);

            newScene.getStylesheets().add(getClass().getResource("/com/beginsecure/application_good/styles/main-style.css").toExternalForm());

            Stage newStage = new Stage();
            newStage.setScene(newScene);
            newStage.setTitle("Main Window - " + newUser.getFirstName() + " " + newUser.getLastName());
            newStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Failed to open new window: " + e.getMessage());
            errorAlert.show();
        }
    }

    public void setLoggedInUser(User user) {
        this.loggedInUser = user;
        if (user != null) {
            initializeData();
        }
    }

    @FXML
    private void openChatWindow() {
        User selectedFriend = friendsTable.getSelectionModel().getSelectedItem();
        if (selectedFriend == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a friend to chat with.");
            alert.show();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/beginsecure/application_good/views/chat-view.fxml"));
            Scene chatScene = new Scene(loader.load());

            ChatController chatController = loader.getController();
            chatController.setService(service);
            chatController.setLoggedInUser(loggedInUser);
            chatController.setChatPartner(selectedFriend);

            service.addObserver(chatController);

            Stage chatStage = new Stage();
            chatStage.setScene(chatScene);
            chatStage.setTitle("Chat with " + selectedFriend.getFirstName());
            chatStage.setOnCloseRequest(event -> service.removeObserver(chatController));
            chatStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Failed to open chat window: " + e.getMessage());
            errorAlert.show();
        }
    }

    @FXML
    private void handlePrevPage() {
        if (currentPageable.getPageNumber() > 0) {
            currentPageable = currentPageable.previous();
            loadFriends();
        }
    }

    @FXML
    private void handleNextPage() {
        currentPageable = currentPageable.next();
        loadFriends();
    }
    @FXML
    private void loadFriends() {
        Page<User> friendsPage = service.getFriendsOnPage(loggedInUser.getId(), currentPageable);

        friendsTable.getItems().setAll((Collection<? extends User>) friendsPage.getElementsOnPage());

        int totalElements = friendsPage.getTotalNumberOfElements();
        int currentPage = currentPageable.getPageNumber() + 1;
        int totalPages = (int) Math.ceil((double) totalElements / currentPageable.getPageSize());

        pageInfoLabel.setText("Page " + currentPage + " of " + totalPages);

        prevPageButton.setDisable(currentPageable.getPageNumber() == 0);
        nextPageButton.setDisable(currentPage >= totalPages);
    }


    @FXML
    private void loadAllUsers() {
        allUsersTable.getItems().setAll(service.findAllUsers());
    }


    @FXML
    private void initialize() {
        friendFirstNameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        friendLastNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        userFirstNameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        userLastNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        prevPageButton.setDisable(true);
        nextPageButton.setDisable(true);
    }
}
