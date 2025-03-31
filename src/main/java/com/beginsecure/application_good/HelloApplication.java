package com.beginsecure.application_good;

import com.beginsecure.application_good.controller.MainWindowController;
import com.beginsecure.application_good.repo.FriendshipDatabaseRepository;
import com.beginsecure.application_good.repo.UserDatabaseRepository;
import com.beginsecure.application_good.service.Service;
import com.beginsecure.application_good.domain.validators.UserValidator;
import com.beginsecure.application_good.domain.validators.FriendshipValidator;
import com.beginsecure.application_good.repo.MessageDatabaseRepository;
import com.beginsecure.application_good.domain.validators.MessageValidator;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HelloApplication extends Application {

//    @Override
//    public void start(Stage primaryStage){
//        try {
//            UserDatabaseRepository userRepo = new UserDatabaseRepository(new UserValidator());
//            FriendshipDatabaseRepository friendshipRepo = new FriendshipDatabaseRepository(new FriendshipValidator());
//            MessageDatabaseRepository messageRepo = new MessageDatabaseRepository(new MessageValidator(), userRepo);
//
//            Service service = new Service(userRepo, friendshipRepo, messageRepo);
//
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("views/main-view.fxml"));
//            Scene mainScene = new Scene(loader.load());
//
//            MainWindowController mainController = loader.getController();
//            mainController.setService(service);
//
//            mainScene.getStylesheets().add(getClass().getResource("styles/main-style.css").toExternalForm());
//
//            primaryStage.setScene(mainScene);
//            primaryStage.setTitle("Social Network");
//            primaryStage.show();
//
//            System.out.println("Connection made successfully!");
//        } catch (Exception e) {
//            e.printStackTrace();
//            System.err.println("Error: " + e.getMessage());
//        }
//    }

    @Override
    public void start(Stage primaryStage) {
        try {
            UserDatabaseRepository userRepo = new UserDatabaseRepository(new UserValidator());
            FriendshipDatabaseRepository friendshipRepo = new FriendshipDatabaseRepository(new FriendshipValidator());
            MessageDatabaseRepository messageRepo = new MessageDatabaseRepository(new MessageValidator(), userRepo);

            Service service = new Service(userRepo, friendshipRepo, messageRepo);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("views/main-view.fxml"));
            Scene mainScene = new Scene(loader.load());

            mainScene.getStylesheets().add(getClass().getResource("styles/main-style.css").toExternalForm());

            MainWindowController mainController = loader.getController();
            mainController.setService(service);

            primaryStage.setScene(mainScene);
            primaryStage.setTitle("Social Network");
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error: " + e.getMessage());
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}
